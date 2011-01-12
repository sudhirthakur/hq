package com.vmware.springsource.hyperic.plugin.gemfire;

import com.vmware.springsource.hyperic.plugin.gemfire.collectors.MemberCollector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.product.LiveDataPlugin;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.jmx.MxUtil;
import org.hyperic.util.config.ConfigResponse;

public class GemFireLiveData extends LiveDataPlugin {

    private static final Log log = LogFactory.getLog(GemFireLiveData.class);
    private static final String[] cmds = {"getMembers", "getDetails", "getSystemID"};

    public Object getData(String command, ConfigResponse config) throws PluginException {
        Object res = null;

        log.debug("[getData] command='" + command + "' config='" + config + "'");
        try {
            MBeanServerConnection mServer = MxUtil.getMBeanServer(config.toProperties());
            log.debug("[getData] mServer=" + mServer);
            if ("getDetails".equals(command)) {
                res = getDetails(mServer);
            } else if ("getMembers".equals(command)) {
                res = getMembers(mServer);
            } else if ("getSystemID".equals(command)) {
                res = getSystemID(mServer);
            } else {
                throw new PluginException("command '" + command + "' not found");
            }
        } catch (Exception e) {
            throw new PluginException(e.getMessage(), e);
        }
        return res;
    }

    public String[] getCommands() {
        return cmds;
    }

    public static String getSystemID(MBeanServerConnection mServer) throws Exception {
        ObjectName mbean = new ObjectName("GemFire:type=MemberInfoWithStatsMBean");
        String id = (String) mServer.getAttribute(mbean, "Id");
        if (id.equalsIgnoreCase("n/a")) {
            getMembers(mServer);    // initialitze the id Attribute on MemberInfoWithStatsMBean
            id = getSystemID(mServer);
        }
        return id;
    }

    private static final Map<String,String> namesCache=new Hashtable();
    private static String[][] getMembers(MBeanServerConnection mServer) throws Exception {
        Object[] args = new Object[0];
        String[] def = new String[0];
        List<String> members = Arrays.asList((String[]) mServer.invoke(new ObjectName("GemFire:type=MemberInfoWithStatsMBean"), "getMembers", args, def));
        List<String[]> names=new ArrayList();
        for(String member :members){
            String name=namesCache.get(member);
            if(name==null){
                Map detail=getMemberDetails(mServer,member);
                name=(String) detail.get("name");
                namesCache.put(member,name);
            }
            String res[]=new String[]{name,member};
            names.add(res);
        }

        //cleanig olds names.
        for(String member: namesCache.keySet()){
            if(!members.contains(member)){
                namesCache.remove(member);
            }
        }

        return names.toArray(new String[0][0]);
    }

    private static Map getDetails(MBeanServerConnection mServer) throws Exception {
        Object[] args = new Object[0];
        String[] def = new String[0];
        String[] members = (String[]) mServer.invoke(new ObjectName("GemFire:type=MemberInfoWithStatsMBean"), "getMembers", args, def);
        Map data = new HashMap();
        for (String member : members) {
            data.put(member, getMemberDetails(mServer, member));
        }
        return data;
    }

    private static Map getMemberDetails(MBeanServerConnection mServer, String member) throws Exception {
        Map details = new HashMap();
        details.putAll(MemberCollector.getMetrics(member, mServer, true));
        return details;
    }
}
