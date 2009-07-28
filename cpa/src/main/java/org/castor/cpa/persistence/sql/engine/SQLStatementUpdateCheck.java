/*
 * Copyright 2006 Assaf Arkin, Thomas Yip, Bruce Snyder, Werner Guttmann, Ralf Joachim, Ahmad Hassan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: SQLStatementUpdateCheck.java 2009-05-30 14:29:32 ahmad $
 */
package org.castor.cpa.persistence.sql.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.core.util.Messages;
import org.castor.cpa.persistence.sql.query.QueryConstants;
import org.castor.persist.ProposedEntity;
import org.exolab.castor.jdo.ObjectDeletedException;
import org.exolab.castor.jdo.ObjectModifiedException;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.engine.SQLColumnInfo;
import org.exolab.castor.jdo.engine.SQLEngine;
import org.exolab.castor.jdo.engine.nature.ClassDescriptorJDONature;
import org.exolab.castor.persist.spi.Identity;
import org.exolab.castor.persist.spi.PersistenceFactory;

/** 
 * SQLStatementUpdatCheck class to check whether the new SQL Update statement has 
 * failed because it has been removed previously from Persistance storage or the object
 * has been modified before. If the object had been modified then ObjectModifiedException
 * is raised and if object had been deleted then ObjectDeletedException is raised.
 * 
 * @author Ahmad
 */
public final class SQLStatementUpdateCheck {
    /** The <a href="http://jakarta.apache.org/commons/logging/">Jakarta
     *  Commons Logging</a> instance used for all logging. */
    private static final Log LOG = LogFactory.getLog(SQLStatementUpdateCheck.class);
    
    /** ThreadLocal attribute for holding prepared statement for a
     *  particular connection that is unique to one thread. */
    private static final ThreadLocal<PreparedStatement> PREPARED_STATEMENT = 
        new ThreadLocal<PreparedStatement>();
   
    /** The name of engine descriptor. */
    private final String _type;
    
    /** Column information for identities specific to the particular engine instance. */
    private final SQLColumnInfo[] _ids;
    
    /** Persistence factory for the database engine the entity is persisted in.
     *  Used to format the SQL statement. */
    private final PersistenceFactory _factory;

    /** The SQL statement for update failure reason check. */
    private String _statement;
    
    /**
     * Constructor.
     * 
     * @param engine SQL engine for all persistence operations at entities of the type this
     *        class is responsible for. Holds all required information of the entity type.    
     */
    public SQLStatementUpdateCheck(final SQLEngine engine, final PersistenceFactory factory) {
        _type = engine.getDescriptor().getJavaClass().getName();
        _ids = engine.getColumnInfoForIdentities();
        _factory = factory;
        
        buildStatement(new ClassDescriptorJDONature(engine.getDescriptor()).getTableName());
    }
    
    private void buildStatement(final String mapTo) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(QueryConstants.SELECT);
        sb.append(QueryConstants.SPACE);
        sb.append(_factory.quoteName(mapTo));
        sb.append(QueryConstants.DOT);
        sb.append(_factory.quoteName(_ids[0].getName()));
        sb.append(QueryConstants.SPACE);
        sb.append(QueryConstants.FROM);
        sb.append(QueryConstants.SPACE);
        sb.append(_factory.quoteName(mapTo));
        sb.append(QueryConstants.SPACE);
        sb.append(QueryConstants.WHERE);
        sb.append(QueryConstants.SPACE);
        
         for (int i = 0; i < _ids.length; i++) {
             if (i != 0) {
                 sb.append(QueryConstants.SPACE);
                 sb.append(QueryConstants.AND);
                 sb.append(QueryConstants.SPACE);
             }
             
             sb.append(_factory.quoteName(mapTo));
             sb.append(QueryConstants.DOT);
             sb.append(_factory.quoteName(_ids[i].getName()));
             sb.append(QueryConstants.ASSIGN);
             sb.append(QueryConstants.PARAMETER);
         }
         
         _statement = sb.toString();
    }
    
    /**
     * This function checks whether the object specified in the statement
     * has been previously removed from the persistent storage or has been modified.
     * If the object had been modified then ObjectModifiedException is raised and 
     * if object had been deleted then ObjectDeletedException is raised.
     * 
     * @param conn An Open JDBC Connection
     * @param identity
     * @param oldentity
     * @throws PersistenceException If a database access error occurs, identity size mismatches,
     *         column length mismatches, ObjectDeletedException if object had been deleted or
     *         ObjectModifiedException if object had been modified before.
     */
    public void updateFailureCheck(final Connection conn, final Identity identity,
            final ProposedEntity oldentity) throws PersistenceException {
        try {      
            prepareStatement(conn);                   
            
            //Binds identity
            bindIdentity(identity);
    
            //Load Data into resultset
            ResultSet resultSet = executeQuery();
        
            if (resultSet.next()) {                     
                String msg = Messages.format("persist.objectModified", _type, identity);
                throw new ObjectModifiedException(msg);
            }
            String msg = Messages.format("persist.objectDeleted", _type, identity);
            throw new ObjectDeletedException(msg);
        } catch (SQLException except) {
            LOG.fatal(Messages.format("jdo.updateCheckFatal", _type,  _statement), except);
            throw new PersistenceException(Messages.format("persist.nested", except), except);
        } finally {
            //close statement
            closeStatement();
        }
    }
    
    /**
     * Prepares the SQL Statement.
     * 
     * @param conn An Open JDBC Connection
     * @throws SQLException If a database access error occurs.
     */
    private void prepareStatement(final Connection conn) 
    throws SQLException { 
        PreparedStatement preparedStatement = conn.prepareStatement(_statement);
        
        // set prepared statement in thread local variable
        PREPARED_STATEMENT.set(preparedStatement);
         
         if (LOG.isTraceEnabled()) {
             LOG.trace(Messages.format("jdo.updateCheck", _type, preparedStatement.toString()));
         }
    }
    
    /**
     * Binds Identity.
     * 
     * @param identity
     * @throws PersistenceException If identity size mismatches
     *  or column length mismatches
     * @throws SQLException If database access error occurs
     */
    private void  bindIdentity(final Identity identity) 
    throws PersistenceException, SQLException {
        // get prepared statement from thread local variable
        PreparedStatement preparedStatement = PREPARED_STATEMENT.get();        
        int offset = 1;
                
        // bind the identity of the row into the preparedStatement
        for (int i = 0; i < _ids.length; i++) {
            preparedStatement.setObject(offset++, _ids[i].toSQL(identity.get(i)));

            if (LOG.isTraceEnabled()) {
                LOG.trace(Messages.format("jdo.bindingIdentity", _ids[i].getName(),
                        _ids[i].toSQL(identity.get(i))));
            }
        }          
    }
    
    /**
     * executeQuery.
     * 
     * @return resultset object containing the results of query
     * @throws SQLException If a database access error occurs
     */
    private ResultSet executeQuery () throws SQLException {
        // get prepared statement from thread local variable
        PreparedStatement preparedStatement = PREPARED_STATEMENT.get();
        ResultSet result = preparedStatement.executeQuery();   
        
        return result;
    }
    
    /**
     * closes the opened statement.
     */
    private void closeStatement() {
        try {
            // get prepared statement from thread local variable
            PreparedStatement preparedStatement = PREPARED_STATEMENT.get();
            
            // Close the insert/select statement
            if (preparedStatement != null) { preparedStatement.close(); }
        } catch (SQLException except2) {
            LOG.warn("Problem closing JDBC statement", except2);
        }
    }
}