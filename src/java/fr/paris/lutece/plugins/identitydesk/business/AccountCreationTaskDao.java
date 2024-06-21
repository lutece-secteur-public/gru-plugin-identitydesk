/*
 * Copyright (c) 2002-2024, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitydesk.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

public class AccountCreationTaskDao implements IAccountCreationTaskDao
{
    private final static String SQL_INSERT = "INSERT INTO identitydesk_account_creation_tasks( customer_id, task_code ) VALUES (?, ?)";
    private final static String SQL_SELECT = "SELECT customer_id, task_code FROM identitydesk_account_creation_tasks WHERE customer_id = ?";
    private final static String SQL_DELETE = "DELETE FROM identitydesk_account_creation_tasks WHERE customer_id = ?";

    @Override
    public void insert( final AccountCreationTask accountCreationTask, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_INSERT ) )
        {
            daoUtil.setString( 1, accountCreationTask.getCustomerId( ) );
            daoUtil.setString( 2, accountCreationTask.getTaskCode( ) );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public AccountCreationTask select( final String customerId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_SELECT ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return new AccountCreationTask( daoUtil.getString( 1 ), daoUtil.getString( 2 ) );
            }
        }
        return null;
    }

    @Override
    public void delete( final String customerId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_DELETE ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeUpdate( );
        }
    }
}