/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2009], Hyperic, Inc.
 * This file is part of HQ.
 *
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.events.server.session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hyperic.hibernate.dialect.HQDialectUtil;
import org.hyperic.hq.application.HQApp;
import org.hyperic.hq.application.Scheduler;
import org.hyperic.hq.application.StartupListener;
import org.hyperic.hq.authz.shared.AuthzConstants;
import org.hyperic.hq.bizapp.server.action.email.EmailFilter;
import org.hyperic.hq.bizapp.server.action.email.EmailRecipient;
import org.hyperic.hq.common.SystemException;
import org.hyperic.hq.common.shared.HQConstants;
import org.hyperic.hq.product.server.session.PluginsDeployedCallback;
import org.hyperic.util.jdbc.DBUtil;

/**
 * The startup listener that schedules the HQ DB Health task 
 */
public class HQDBHealthStartupListener 
    implements StartupListener, PluginsDeployedCallback {

    /**
     * The period (in msec) at which heart beats are dispatched by the 
     * Heart Beat Service.
     */
    public static final int HEALTH_CHECK_PERIOD_MILLIS = 15*1000;
    
    private final Log _log = 
        LogFactory.getLog(HQDBHealthStartupListener.class);
        
    /**
     * @see org.hyperic.hq.application.StartupListener#hqStarted()
     */
    public void hqStarted() {
        // We want to start the health check only after all plugins 
        // have been deployed since this is when the server starts accepting 
        // metrics from agents.
        HQApp.getInstance().
            registerCallbackListener(PluginsDeployedCallback.class, this);
    }
    
    /**
     * @see org.hyperic.hq.product.server.session.PluginsDeployedCallback#pluginsDeployed(java.util.List)
     */
    public void pluginsDeployed(List plugins) {
        _log.info("Scheduling HQ DB Health to perform a health check every " +
                   (HEALTH_CHECK_PERIOD_MILLIS/1000) + " sec");
        
        Scheduler scheduler = HQApp.getInstance().getScheduler();
        
        scheduler.scheduleAtFixedRate(new HQDBHealthTask(), 
                                      Scheduler.NO_INITIAL_DELAY, 
                                      HEALTH_CHECK_PERIOD_MILLIS);        
    }
    
    private static class HQDBHealthTask implements Runnable {
        
        private final Log _log = LogFactory.getLog(HQDBHealthTask.class);
                
        private long healthOkStartTime = 0; // time of first OK health check
        private long lastHealthOkTime = 0; // time of last OK health check
        private int numOfHealthCheckFailures = 0;   
        private final String HQADMIN_EMAIL_SQL = "SELECT email_address FROM EAM_SUBJECT WHERE id = " 
                                                    + AuthzConstants.rootSubjectId;
        private String hqadminEmail = null;

        public void run() {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBUtil.getConnByContext(new InitialContext(), 
                                               HQConstants.DATASOURCE);
                stmt = conn.createStatement();
                
                if (healthOkStartTime == 0) {
                    // get timestamp to check overall db health
                    Dialect dialect = HQDialectUtil.getDialect(conn);
                    rs = stmt.executeQuery(dialect.getCurrentTimestampSelectString());
                    
                    if (rs.next()) {
                        rs.getString(1);
                        healthOkStartTime = System.currentTimeMillis();
                    }
                } else {
                    // get latest email address to send to in case of db failures
                    rs = stmt.executeQuery(HQADMIN_EMAIL_SQL);
                
                    if (rs.next()) {
                        hqadminEmail = rs.getString(1);
                    }
                }
                lastHealthOkTime = System.currentTimeMillis();
                numOfHealthCheckFailures = 0;
                _log.debug("HQ DB Health: OK since " + new Date(healthOkStartTime));                
            } catch (Throwable t) {
                numOfHealthCheckFailures++;
                healthOkStartTime = 0;
                
                String status = "HQ DB Health: Failed. Attempt #" + numOfHealthCheckFailures
                                    + ". Last successful check at " + new Date(lastHealthOkTime);
                                
                if (numOfHealthCheckFailures < 8) {
                    _log.error(status + ". Checking again in "
                                + (HEALTH_CHECK_PERIOD_MILLIS/1000) + " sec", t);
                } else {
                    // shut down HQ if the database health check fails after 2 minutes
                    _log.error(status + ". Shutting down HQ.", t);
                    try {
                        shutdownNotify(t);
                    } catch (Throwable t2) {
                        // catch all so that HQ can shutdown
                    }
                    System.exit(1);
                }
            } finally {
                DBUtil.closeJDBCObjects(HQDBHealthTask.class, conn, stmt, rs);
            }            
        }
        
        /**
         * email notify of shutdown
         */
        private void shutdownNotify(Throwable t) {
            try {
                InternetAddress addr = new InternetAddress(hqadminEmail);
                EmailRecipient rec = new EmailRecipient(addr, false);
                
                EmailRecipient[] addresses = new EmailRecipient[] { rec };
                
                String[] body = new String[addresses.length];                
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                
                Arrays.fill(body, 
                            "Hyperic HQ Shutdown was initiated because of the following error:\n\n"
                                + sw.toString());
                
                EmailFilter.sendEmail(addresses, "Hyperic HQ Shutdown Notification", 
                                      body, null, null);
            } catch (AddressException e) {
                _log.error("Invalid email address: " + hqadminEmail);
            } catch (SystemException e) {
                _log.error("HQ services not available for sending emails");
            }            
        }
        
    }

}
