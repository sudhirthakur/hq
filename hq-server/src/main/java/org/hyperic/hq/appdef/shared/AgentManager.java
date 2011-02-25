/**
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 *  "derived work".
 *
 *  Copyright (C) [2009-2010], VMware, Inc.
 *  This file is part of HQ.
 *
 *  HQ is free software; you can redistribute it and/or modify
 *  it under the terms version 2 of the GNU General Public License as
 *  published by the Free Software Foundation. This program is distributed
 *  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *
 */
package org.hyperic.hq.appdef.shared;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hyperic.hibernate.PageInfo;
import org.hyperic.hq.agent.AgentConnectionException;
import org.hyperic.hq.agent.AgentRemoteException;
import org.hyperic.hq.appdef.Agent;
import org.hyperic.hq.appdef.server.session.AgentManagerImpl;
import org.hyperic.hq.appdef.server.session.AgentPluginStatus;
import org.hyperic.hq.appdef.server.session.AgentPluginStatusEnum;
import org.hyperic.hq.appdef.server.session.AgentSortField;
import org.hyperic.hq.appdef.server.session.AgentConnections.AgentConnection;
import org.hyperic.hq.appdef.shared.resourceTree.ResourceTree;
import org.hyperic.hq.authz.server.session.AuthzSubject;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.bizapp.shared.lather.PluginReport_args;
import org.hyperic.hq.product.Plugin;
import org.hyperic.util.ConfigPropertyException;

/**
 * Local interface for AgentManager.
 */
public interface AgentManager {

    public void removeAgent(Agent agent);

    /**
     * Get a list of all the entities which can be serviced by an Agent.
     */
    public ResourceTree getEntitiesForAgent(AuthzSubject subject, String agentToken) throws AgentNotFoundException,
        PermissionException;

    /**
     * Get a paged list of agents in the system.
     * @param pInfo a pager object, with an {@link AgentSortField} sort field
     * @return a list of {@link Agent}s
     */
    public List<Agent> findAgents(PageInfo pInfo);

    /**
     * Get a list of all the agents in the system
     */
    public List<Agent> getAgents();

    /**
     * Get a count of all the agents in the system
     */
    public int getAgentCount();

    /**
     * Get a count of the agents which are actually used (e. have platforms)
     */
    public int getAgentCountUsed();
    
    /**
     * @param aeids {@link Collection} of {@link AppdefEntityID}s
     * @return Map of {@link Agent} to {@link Collection} of ${AppdefEntityID}s
     *
     */
    public Map<Integer,Collection<AppdefEntityID>> getAgentMap(Collection<AppdefEntityID> aeids);

    /**
     * Create a new Agent object. The type of the agent that is created is the
     * 'hyperic-hq-remoting' agent. This type of agent may be configured to use
     * either a bidirectional or unidirectional transport.
     */
    public Agent createNewTransportAgent(String address, Integer port, String authToken, String agentToken,
                                         String version, boolean unidirectional) throws AgentCreateException;

    /**
     * Create a new Agent object. The type of the agent that is created is the
     * legacy 'covalent-eam' type.
     */
    public Agent createLegacyAgent(String address, Integer port, String authToken, String agentToken, String version)
        throws AgentCreateException;

    /**
     * Update an existing Agent given the old agent token. The auth token will
     * be reset. The type of the agent that is updated is the
     * 'hyperic-hq-remoting' agent. This type of agent may be configured to use
     * either a bidirectional or unidirectional transport.
     * @return An Agent object representing the updated agent
     */
    public Agent updateNewTransportAgent(String agentToken, String ip, int port, String authToken, String version,
                                         boolean unidirectional) throws AgentNotFoundException;

    /**
     * Update an existing Agent given the old agent token. The auth token will
     * be reset. The type of the agent that is updated is the legacy
     * 'covalent-eam' type.
     * @return An Agent object representing the updated agent
     */
    public Agent updateLegacyAgent(String agentToken, String ip, int port, String authToken, String version)
        throws AgentNotFoundException;

    /**
     * Update an existing Agent given an IP and port. The type of the agent that
     * is updated is the 'hyperic-hq-remoting' agent. This type of agent may be
     * configured to use either a bidirectional or unidirectional transport.
     * @return An Agent object representing the updated agent
     */
    public Agent updateNewTransportAgent(String ip, int port, String authToken, String agentToken, String version,
                                         boolean unidirectional) throws AgentNotFoundException;

    /**
     * Update an existing Agent given an IP and port. The type of the agent that
     * is updated is the legacy 'covalent-eam' type.
     * @return An Agent object representing the updated agent
     */
    public Agent updateLegacyAgent(String ip, int port, String authToken, String agentToken, String version)
        throws AgentNotFoundException;

    public List<Agent> findAgentsByIP(String ip);

    /**
     * Update an existing agent's IP and port based on an agent token. The type
     * of the agent that is updated is the 'hyperic-hq-remoting' agent. This
     * type of agent may be configured to use either a bidirectional or
     * unidirectional transport.
     * @param agentToken Token that the agent uses to connect to HQ
     * @param ip The new IP address
     * @param port The new port
     * @return An Agent object representing the updated agent
     */
    public Agent updateNewTransportAgent(String agentToken, String ip, int port, boolean unidirectional)
        throws AgentNotFoundException;

    /**
     * Update an existing agent's IP and port based on an agent token. The type
     * of the agent that is updated is the legacy 'covalent-eam' type.
     * @param agentToken Token that the agent uses to connect to HQ
     * @param ip The new IP address
     * @param port The new port
     * @return An Agent object representing the updated agent
     */
    public Agent updateLegacyAgent(String agentToken, String ip, int port) throws AgentNotFoundException;

    /**
     * Find an agent by the token which is Required for the agent to send when
     * it connects.
     */
    public void checkAgentAuth(String agentToken) throws AgentUnauthorizedException;

    public AgentConnection getAgentConnection(String method, String connIp, Integer agentId);

    public void disconnectAgent(AgentConnection a);

    public Collection<Agent> getConnectedAgents();

    /**
     * Find an agent listening on a specific IP & port
     */
    public Agent getAgent(String ip, int port) throws AgentNotFoundException;

    /**
     * Find an agent by agent token.
     * @param agentToken the agent token to look for
     * @return An Agent representing the agent that has the given token.
     */
    public Agent getAgent(String agentToken) throws AgentNotFoundException;

    /**
     * Determine if the agent token is already assigned to another agent.
     * @param agentToken The agent token.
     * @return <code>true</code> if the agent token is unique;
     *         <code>false</code> if it is already assigned to an agent.
     */
    public boolean isAgentTokenUnique(String agentToken);

    public Agent findAgent(Integer id);

    /**
     * Get an Agent by id.
     */
    public Agent getAgent(Integer id);

    /**
     * Find an agent which can service the given entity ID
     * @return An agent which is set to manage the specified ID
     */
    public Agent getAgent(AppdefEntityID aID) throws AgentNotFoundException;

    /**
     * Return the bundle that is currently running on a give agent. The returned
     * bundle name may be parsed to retrieve the current agent version.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @return The bundle name currently running.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue the query.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     */
    public String getCurrentAgentBundle(AuthzSubject subject, AppdefEntityID aid) throws PermissionException,
        AgentNotFoundException, AgentRemoteException, AgentConnectionException;

    /**
     * Upgrade an agent asynchronously including agent restart. This operation
     * blocks long enough only to do some basic failure condition checking
     * (permissions, agent existence, file existence, config property existence)
     * then delegates the actual commands to the Zevent subsystem.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param bundleFileName The agent bundle name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent upgrade.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     * @throws InterruptedException if enqueuing the Zevent is interrupted.
     */
    public void upgradeAgentAsync(AuthzSubject subject, AppdefEntityID aid, String bundleFileName)
        throws PermissionException, FileNotFoundException, AgentNotFoundException, ConfigPropertyException,
        InterruptedException;

    /**
     * Upgrade an agent synchronously including agent restart. This operation is
     * composed of transferring the agent bundle, upgrading the agent, and
     * restarting the agent, in that order.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param bundleFileName The agent bundle name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent upgrade command.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     */
    public void upgradeAgent(AuthzSubject subject, AppdefEntityID aid, String bundleFileName)
        throws PermissionException, AgentNotFoundException, AgentConnectionException, AgentRemoteException,
        FileNotFoundException, ConfigPropertyException, IOException;

    /**
     * Transfer asynchronously an agent bundle residing on the HQ server to an
     * agent. This operation blocks long enough only to do some basic failure
     * condition checking (permissions, agent existence, file existence, config
     * property existence) then delegates the actual file transfer to the Zevent
     * subsystem.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param bundleFileName The agent bundle name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent bundle transfer.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     * @throws InterruptedException if enqueuing the Zevent is interrupted.
     */
    public void transferAgentBundleAsync(AuthzSubject subject, AppdefEntityID aid, String bundleFileName)
        throws PermissionException, AgentNotFoundException, FileNotFoundException, ConfigPropertyException,
        InterruptedException;

    /**
     * Transfer an agent bundle residing on the HQ server to an agent.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param bundleFileName The agent bundle name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent bundle transfer.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     */
    public void transferAgentBundle(AuthzSubject subject, AppdefEntityID aid, String bundleFileName)
        throws PermissionException, AgentNotFoundException, AgentConnectionException, AgentRemoteException,
        FileNotFoundException, IOException, ConfigPropertyException;

    /**
     * Transfer an agent plugin residing on the HQ server to an agent.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param plugin The plugin name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent plugin transfer.
     * @throws FileNotFoundException if the plugin is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     */
    public void transferAgentPlugin(AuthzSubject subject, AppdefEntityID aid, String plugin)
        throws PermissionException, AgentConnectionException, AgentNotFoundException, AgentRemoteException,
        FileNotFoundException, IOException, ConfigPropertyException;

    /**
     * Transfer an agent plugin residing on the HQ server to an agent. The
     * transfer is performed asynchronously by placing on the Zevent queue and
     * results in an agent restart.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param plugin The plugin name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent plugin transfer.
     * @throws FileNotFoundException if the plugin is not found on the HQ
     *         server.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws InterruptedException if enqueuing the Zevent is interrupted.
     */
    public void transferAgentPluginAsync(AuthzSubject subject, AppdefEntityID aid, String plugin)
        throws PermissionException, FileNotFoundException, AgentNotFoundException, InterruptedException;

    /**
     * Upgrade to the specified agent bundle residing on the HQ agent.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @param bundleFileName The agent bundle name.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent bundle transfer.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     */
    public void upgradeAgentBundle(AuthzSubject subject, AppdefEntityID aid, String bundleFileName)
        throws PermissionException, AgentNotFoundException, AgentConnectionException, AgentRemoteException,
        FileNotFoundException, IOException, ConfigPropertyException;

    /**
     * Restarts the specified agent using the Java Service Wrapper.
     * @param subject The subject issuing the request.
     * @param agentId The agent id.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent bundle transfer.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     */
    public void restartAgent(AuthzSubject subject, Integer agentId) throws PermissionException,
        AgentNotFoundException, AgentConnectionException, AgentRemoteException, FileNotFoundException, IOException,
        ConfigPropertyException;

    public void restartAgent(AuthzSubject subject, AppdefEntityID aeid) throws PermissionException,
        AgentNotFoundException, AgentConnectionException, AgentRemoteException, FileNotFoundException, IOException,
        ConfigPropertyException;

    /**
     * Pings the specified agent.
     * @see AgentManagerImpl#pingAgent(AuthzSubject, Agent)
     */
    public long pingAgent(AuthzSubject subject, AppdefEntityID id) throws AgentNotFoundException, PermissionException,
        AgentConnectionException, IOException, ConfigPropertyException, AgentRemoteException;

    /**
     * Pings the specified agent.
     * @param subject The subject issuing the request.
     * @param aid The agent id.
     * @return the time it took (in milliseconds) for the round-trip time of the
     *         request to the agent.
     * @throws PermissionException if the subject does not have proper
     *         permissions to issue an agent bundle transfer.
     * @throws FileNotFoundException if the agent bundle is not found on the HQ
     *         server.
     * @throws IOException if an I/O error occurs, such as failing to calculate
     *         the file MD5 checksum.
     * @throws AgentRemoteException if an exception occurs on the remote agent
     *         side.
     * @throws AgentConnectionException if the connection to the agent fails.
     * @throws AgentNotFoundException if no agent exists with the given agent
     *         id.
     * @throws ConfigPropertyException if the server configuration cannot be
     *         retrieved.
     */
    public long pingAgent(AuthzSubject subject, Agent agent) throws PermissionException, AgentNotFoundException,
        AgentConnectionException, AgentRemoteException, IOException, ConfigPropertyException;

// XXX javadoc!
    public void updateAgentPluginStatus(PluginReport_args arg);

// XXX javadoc!
    public void updateAgentPluginStatusInBackground(PluginReport_args arg);

// XXX javadoc!
    public void transferAgentPlugins(AuthzSubject subj, Integer agentId, Collection<String> jarNames)
    throws PermissionException, AgentConnectionException, AgentNotFoundException,
           AgentRemoteException, FileNotFoundException, IOException, ConfigPropertyException;
    
// XXX javadoc!
    public long getNumAutoUpdatingAgents();
    
// XXX javadoc!
    public List<Plugin> getAllPlugins();

// XXX javadoc!
    public Map<Plugin, Collection<AgentPluginStatus>> getOutOfSyncAgentsByPlugin();

// XXX javadoc!
    public Collection<String> getOutOfSyncPluginNamesByAgentId(Integer agentId);

// XXX javadoc!
    public void updateAgentPluginSyncStatusInNewTran(AgentPluginStatusEnum s, Integer agentId,
                                                     Collection<Plugin> plugins);

}
