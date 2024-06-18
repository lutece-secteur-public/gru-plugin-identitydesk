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
package fr.paris.lutece.plugins.identitydesk.dto;

import fr.paris.lutece.plugins.identitydesk.business.AccountCreationTask;
import fr.paris.lutece.plugins.identitydesk.business.AccountCreationTaskHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;

public class ExtendedIdentityDto extends IdentityDto
{
    private AccountCreationTask accountCreationTask;

    public ExtendedIdentityDto( final IdentityDto identityDto )
    {
        this.setCustomerId( identityDto.getCustomerId( ) );
        this.setConnectionId( identityDto.getConnectionId( ) );
        this.setQuality( identityDto.getQuality( ) );
        this.setLastUpdateDate( identityDto.getLastUpdateDate( ) );
        this.setExternalCustomerId( identityDto.getExternalCustomerId( ) );
        this.setDuplicateDefinition( identityDto.getDuplicateDefinition( ) );
        this.setMonParisActive( identityDto.getMonParisActive( ) );
        this.setCreationDate( identityDto.getCreationDate( ) );
        this.setExpiration( identityDto.getExpiration( ) );
        this.setMerge( identityDto.getMerge( ) );
        this.setSuspicious( identityDto.isSuspicious( ) );
        this.setConsolidate( identityDto.getConsolidate( ) );
        this.setMatchedDuplicateRuleCode( identityDto.getMatchedDuplicateRuleCode( ) );
        this.getAttributes( ).addAll( identityDto.getAttributes( ) );
    }

    public AccountCreationTask getAccountCreationTask( )
    {
        return accountCreationTask;
    }

    public void setAccountCreationTask( AccountCreationTask accountCreationTask )
    {
        this.accountCreationTask = accountCreationTask;
    }

    public static ExtendedIdentityDto from( final IdentityDto identityDto )
    {
        final ExtendedIdentityDto extendedIdentityDto = new ExtendedIdentityDto( identityDto );
        extendedIdentityDto.setAccountCreationTask( AccountCreationTaskHome.get( extendedIdentityDto.getCustomerId( ) ) );
        return extendedIdentityDto;
    }
}
