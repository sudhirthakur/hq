/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2008], Hyperic, Inc.
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

package org.hyperic.hq.ui.action.resource.server.inventory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.hyperic.hq.appdef.shared.ServerValue;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.ui.action.WorkflowPrepareAction;
import org.hyperic.hq.ui.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 *
 */
public class ServerGeneralFormPrepareAction
    extends WorkflowPrepareAction {

    private AppdefBoss appdefBoss;

    @Autowired
    public ServerGeneralFormPrepareAction(AppdefBoss appdefBoss) {
        super();
        this.appdefBoss = appdefBoss;
    }

    /**
     * Retrieve this data and store it in the <code>GroupForm</code>:
     * 
     */
    public ActionForward workflow(ComponentContext context, ActionMapping mapping, ActionForm form,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        ServerForm serverForm = (ServerForm) form;

        Integer sessionId = RequestUtils.getSessionId(request);

        Integer resourceId = RequestUtils.getResourceId(request);

        ServerValue server = appdefBoss.findServerById(sessionId.intValue(), resourceId);

        serverForm.loadResourceValue(server);

        RequestUtils.setResource(request, server);

        return null;
    }
}
