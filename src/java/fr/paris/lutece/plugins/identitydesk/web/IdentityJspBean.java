/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.identitydesk.web;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitydesk/", right = "IDENTITYDESK_MANAGEMENT" )
public class IdentityJspBean extends ManageIdentitiesJspBean
{
    private static final long serialVersionUID = 6053504380426222888L;
    // Templates
    private static final String TEMPLATE_SEARCH_IDENTITIES = "/admin/plugins/identitydesk/search_identities.html";
    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitydesk/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitydesk/modify_identity.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitydesk.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitydesk.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitydesk.create_identity.pageTitle";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_QUERY_SEARCH_ATTRIBUTES = "query_search_attributes";
    private static final String JSP_MANAGE_IDENTITIES = "jsp/admin/plugins/identitydesk/ManageIdentities.jsp";
    private static final String MARK_AUTOCOMPLETE_CITY_ENDPOINT = "autocomplete_city_endpoint";
    private static final String MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT = "autocomplete_country_endpoint";
    private static final String MARK_RETURN_URL = "return_url";

    // Views
    private static final String VIEW_MANAGE_IDENTITIES = "manageIdentitys";
    private static final String VIEW_CREATE_IDENTITY = "createIdentity";
    private static final String VIEW_MODIFY_IDENTITY = "modifyIdentity";

    // Actions
    private static final String ACTION_CREATE_IDENTITY = "createIdentity";
    private static final String ACTION_MODIFY_IDENTITY = "modifyIdentity";

    // Session variable to store working values
    private final List<SearchAttributeDto> _searchAttributes = new ArrayList<>( );
    private ServiceContractDto _serviceContract;

    private final IdentityService _identityService = SpringContextService.getBean( "identityService.rest.httpAccess" );

    private final String _autocompleteCityEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.city.endpoint" );
    private final String _autocompleteCountryEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.country.endpoint" );
    private final String _defaultClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );
    private final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitydesk.attribute.order" ).split( "," ) );
    private final List<String> _searchAttributeKeyStrictList = Arrays
            .asList( AppPropertiesService.getProperty( "identitydesk.search.strict.attributes" ).split( "," ) );

    /**
     * Process the data to send the search request and returns the identity search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( value = VIEW_MANAGE_IDENTITIES, defaultView = true )
    public String getManageIdentitys( HttpServletRequest request )
    {
        final List<QualifiedIdentity> qualifiedIdentities = new ArrayList<>( );

        final String clientCode = getClientCode( request );
        try
        {
            final ServiceContractSearchResponse response = _identityService.getServiceContract( clientCode );
            _serviceContract = response.getServiceContract( );
            sortServiceContractAttributes( );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
            addError( "Une erreur est survenue pendant la récupération du contrat de service." );
        }
        if ( _serviceContract != null )
        {
            final String customerId = _searchAttributes.stream( ).filter( a -> a.getKey( ).equals( "customer_id" ) ).map( SearchAttributeDto::getValue )
                    .findFirst( ).orElse( null );
            if ( collectSearchAttributes( request ) && CollectionUtils.isNotEmpty( _searchAttributes ) )
            {
                if ( StringUtils.isNotBlank( customerId ) )
                {
                    try
                    {
                        final QualifiedIdentity identity = getIdentityFromCustomerId( customerId, clientCode, QualifiedIdentity.class );
                        qualifiedIdentities.add( identity );
                    }
                    catch( final IdentityStoreException e )
                    {
                        AppLogService.error( "Error while retrieving the identity [customerId = " + customerId + "].", e );
                        addError( "Une erreur est survenue pendant la récupération de l'identité." );
                    }
                }
                else
                {
                    final IdentitySearchRequest searchRequest = new IdentitySearchRequest( );
                    final SearchDto search = new SearchDto( );
                    searchRequest.setSearch( search );
                    search.setAttributes( _searchAttributes );
                    try
                    {
                        final IdentitySearchResponse searchResponse = _identityService.searchIdentities( searchRequest, clientCode );
                        qualifiedIdentities.addAll( searchResponse.getIdentities( ) );
                        if ( qualifiedIdentities.isEmpty( ) )
                        {
                            addWarning( "Aucun résultat pour votre recherche." );
                        }
                    }
                    catch( final IdentityStoreException e )
                    {
                        AppLogService.error( "Error while searching identities [IdentitySearchRequest = " + searchRequest + "].", e );
                        addError( "Une erreur est survenue pendant la recherche d'identités." );
                    }
                }
            }
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, qualifiedIdentities, JSP_MANAGE_IDENTITIES );
        model.put( MARK_QUERY_SEARCH_ATTRIBUTES, _searchAttributes );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        markReturnUrl( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
    }

    /**
     * Returns the form to create a identity
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( VIEW_CREATE_IDENTITY )
    public String getCreateIdentity( HttpServletRequest request )
    {
        final Identity identity = initNewIdentity( request );
        final String clientCode = getClientCode( request );
        try
        {
            final ServiceContractSearchResponse response = _identityService.getServiceContract( clientCode );
            _serviceContract = response.getServiceContract( );
            sortServiceContractAttributes( );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
            addError( "Erreur lors de la récupération du contrat de service" );
            return getManageIdentitys( request );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, identity );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        markReturnUrl( request, model );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_CREATE_IDENTITY, model );
    }

    /**
     * Process the data capture form of a new identity
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_IDENTITY )
    public String doCreateIdentity( HttpServletRequest request )
    {
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        try
        {
            final Identity identity = initNewIdentity( request );
            if ( identity.getAttributes( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getCertificationProcess( ) ) ) )
            {
                addWarning( "Un niveau de certification doit obligatoirement être sélectionné pour chaque attribut renseigné." );
                return getCreateIdentity( request );
            }
            identityChangeRequest.setIdentity( identity );
            identityChangeRequest.setOrigin( this.getAuthor( ) );

            final IdentityChangeResponse response = _identityService.createIdentity( identityChangeRequest, getClientCode( request ) );
            if ( response.getStatus( ) != IdentityChangeStatus.CREATE_SUCCESS )
            {
                if ( response.getStatus( ) == IdentityChangeStatus.FAILURE )
                {
                    addError( response.getMessage( ) );
                }
                else
                {
                    addWarning( response.getMessage( ) );
                }
                return getCreateIdentity( request );
            }
            else
            {
                addInfo( "Identité créée avec succès" );
                _searchAttributes.add( new SearchAttributeDto( "customer_id", response.getCustomerId( ), true ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while creating the identity [IdentityChangeRequest = " + identityChangeRequest + "].", e );
            addError( "Erreur lors de la création de l'identité." );
            return getCreateIdentity( request );
        }
        return getManageIdentitys( request );
    }

    /**
     * Returns the form to update info about a identity
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_IDENTITY )
    public String getModifyIdentity( HttpServletRequest request )
    {
        final String customerId = request.getParameter( "customer_id" );
        final QualifiedIdentity qualifiedIdentity;
        final String clientCode = getClientCode( request );
        try
        {
            qualifiedIdentity = this.getIdentityFromCustomerId( customerId, clientCode, QualifiedIdentity.class );
            if ( qualifiedIdentity == null )
            {
                addError( "Erreur lors de la récupération de l'identité sélectionnée" );
                return getManageIdentitys( request );
            }
            final ServiceContractSearchResponse contractResponse = _identityService.getServiceContract( clientCode );
            _serviceContract = contractResponse.getServiceContract( );
            sortServiceContractAttributes( );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
            addError( "Erreur lors de la récupération de l'identité sélectionnée" );
            return getManageIdentitys( request );
        }
        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, qualifiedIdentity );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        markReturnUrl( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_IDENTITY, TEMPLATE_MODIFY_IDENTITY, model );
    }

    /**
     * Process the change form of a identity
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_IDENTITY )
    public String doModifyIdentity( HttpServletRequest request )
    {
        final String customerId = request.getParameter( "customer_id" );
        final IdentityChangeRequest changeRequest = new IdentityChangeRequest( );
        try
        {
            final Identity identityToUpdate = this.getIdentityFromCustomerId( customerId, getClientCode( request ), Identity.class );
            if ( identityToUpdate == null )
            {
                addError( "Erreur lors de la récupération de l'identité sélectionnée" );
                return getManageIdentitys( request );
            }
            final Identity identityFromParams = this.initNewIdentity( request );
            if ( identityFromParams.getAttributes( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getCertificationProcess( ) ) ) )
            {
                addWarning( "Un niveau de certification doit obligatoirement être sélectionné pour chaque attribut renseigné." );
                return getModifyIdentity( request );
            }

            final List<CertifiedAttribute> updatedAttributes = identityFromParams.getAttributes( ).stream( ).map( newAttr -> {
                final CertifiedAttribute oldAttr = identityToUpdate.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( newAttr.getKey( ) ) )
                        .findFirst( ).orElse( null );
                if ( oldAttr != null && oldAttr.getValue( ).equals( newAttr.getValue( ) )
                        && Objects.equals( oldAttr.getCertificationProcess( ), newAttr.getCertificationProcess( ) ) )
                {
                    return null;
                }
                return newAttr;
            } ).filter( Objects::nonNull ).collect( Collectors.toList( ) );

            if ( CollectionUtils.isEmpty( updatedAttributes ) )
            {
                addInfo( "Aucune modification d'attribut détectée, identité non mise à jour." );
                _searchAttributes.add( new SearchAttributeDto( "customer_id", customerId, true ) );
                return getManageIdentitys( request );
            }

            identityToUpdate.setAttributes( updatedAttributes );
            changeRequest.setIdentity( identityToUpdate );
            changeRequest.setOrigin( this.getAuthor( ) );
            final IdentityChangeResponse response = _identityService.updateIdentity( identityToUpdate.getCustomerId( ), changeRequest,
                    getClientCode( request ) );
            if ( response.getStatus( ) != IdentityChangeStatus.UPDATE_SUCCESS && response.getStatus( ) != IdentityChangeStatus.UPDATE_INCOMPLETE_SUCCESS )
            {
                if ( response.getStatus( ) == IdentityChangeStatus.FAILURE )
                {
                    addError( "Erreur lors de la mise à jour : " + response.getMessage( ) );
                }
                else
                {
                    addWarning( "Status de la mise à jour : " + response.getStatus( ).getLabel( ) + " : " + response.getMessage( ) );
                }
                return getModifyIdentity( request );
            }
            else
            {
                addInfo( "Identité modifiée avec succès" );
                _searchAttributes.add( new SearchAttributeDto( "customer_id", customerId, true ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while updating the identity [IdentityChangeRequest = " + changeRequest + "].", e );
            addError( "Erreur lors de la modification de l'identité." );
            return getModifyIdentity( request );
        }
        return getManageIdentitys( request );
    }

    private boolean collectSearchAttributes( final HttpServletRequest request )
    {
        final List<SearchAttributeDto> searchList = _serviceContract.getAttributeDefinitions( ).stream( ).map( AttributeDefinitionDto::getKeyName )
                .map( attrKey -> {
                    final String value = request.getParameter( attrKey );
                    if ( value != null )
                    {
                        return new SearchAttributeDto( attrKey, value.trim( ), _searchAttributeKeyStrictList.contains( attrKey ) );
                    }
                    return null;
                } ).collect( Collectors.toList( ) );

        if ( !searchList.contains( null ) )
        {
            _searchAttributes.clear( );
            _searchAttributes.addAll( searchList.stream( ).filter( s -> StringUtils.isNotBlank( s.getValue( ) ) ).collect( Collectors.toList( ) ) );
            final Optional<SearchAttributeDto> login = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "login" ) ).findFirst( );
            if ( !login.isPresent( ) || StringUtils.isBlank( login.get( ).getValue( ) ) )
            {
                final Optional<SearchAttributeDto> firstname = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "first_name" ) ).findFirst( );
                final Optional<SearchAttributeDto> familyname = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "family_name" ) ).findFirst( );
                final Optional<SearchAttributeDto> birthdate = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "birthdate" ) ).findFirst( );
                if ( ( !firstname.isPresent( ) || StringUtils.isBlank( firstname.get( ).getValue( ) ) )
                        || ( !familyname.isPresent( ) || StringUtils.isBlank( familyname.get( ).getValue( ) ) )
                        || ( !birthdate.isPresent( ) || StringUtils.isBlank( birthdate.get( ).getValue( ) ) ) )
                {
                    addInfo( "Pour lancer une recherche, merci de renseigner au minimum soit :"
                            + "<ul><li>Le critère Login</li><li>Les critères Prénoms + Nom de naissance + Date de naissance</li></ul>" );
                    return false;
                }
            }
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( "birthplace" ) ) )
            {
                final String value = request.getParameter( "birthplace_code" );
                _searchAttributes.add( new SearchAttributeDto( "birthplace_code", value, _searchAttributeKeyStrictList.contains( "birthplace_code" ) ) );
            }
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( "birthcountry" ) ) )
            {
                final String value = request.getParameter( "birthcountry_code" );
                _searchAttributes.add( new SearchAttributeDto( "birthcountry_code", value, _searchAttributeKeyStrictList.contains( "birthcountry_code" ) ) );
            }
            return true;
        }
        return false;
    }

    private String getClientCode( final HttpServletRequest request )
    {
        String clientCode = request.getParameter( "client_code" );
        if ( StringUtils.isBlank( clientCode ) )
        {
            return _defaultClientCode;
        }
        return clientCode;
    }

    private void markReturnUrl( final HttpServletRequest request, final Map<String, Object> model )
    {
        final String returnUrl = request.getParameter( "return_url" );
        if ( StringUtils.isNotBlank( returnUrl ) )
        {
            model.put( MARK_RETURN_URL, returnUrl );
        }
    }

    private Identity initNewIdentity( final HttpServletRequest request )
    {
        final Identity identity = new Identity( );
        request.getParameterMap( ).entrySet( ).stream( )
                .filter( entry -> !entry.getKey( ).startsWith( "action_" ) && !entry.getKey( ).startsWith( "view_" ) && !entry.getKey( ).endsWith( "-certif" )
                        && !entry.getKey( ).startsWith( "birthplace" ) && !entry.getKey( ).startsWith( "birthcountry" )
                        && !entry.getKey( ).equals( "customer_id" ) && !entry.getKey( ).equals( "client_code" ) && !entry.getKey( ).equals( "return_url" ) )
                .filter( entry -> entry.getValue( ) != null && entry.getValue( ).length == 1 && StringUtils.isNotBlank( entry.getValue( ) [0] ) )
                .forEach( entry -> identity.getAttributes( )
                        .add( initAttribute( entry.getKey( ), entry.getValue( ) [0], request.getParameter( entry.getKey( ) + "-certif" ) ) ) );

        final String birthplace = request.getParameter( "birthplace" );
        final String birthplace_code = request.getParameter( "birthplace_code" );
        final String birthcountry = request.getParameter( "birthcountry" );
        final String birthcountry_code = request.getParameter( "birthcountry_code" );

        if ( StringUtils.isNotBlank( birthplace ) )
        {
            identity.getAttributes( ).add( initAttribute( "birthplace", birthplace, request.getParameter( "birthplace-certif" ) ) );
            identity.getAttributes( ).add( initAttribute( "birthplace_code", birthplace_code, request.getParameter( "birthplace-certif" ) ) );
        }
        if ( StringUtils.isNotBlank( birthcountry ) )
        {
            identity.getAttributes( ).add( initAttribute( "birthcountry", birthcountry, request.getParameter( "birthcountry-certif" ) ) );
            identity.getAttributes( ).add( initAttribute( "birthcountry_code", birthcountry_code, request.getParameter( "birthcountry-certif" ) ) );
        }

        return identity;
    }

    private CertifiedAttribute initAttribute( final String key, final String value, final String certificationCode )
    {
        final CertifiedAttribute attr = new CertifiedAttribute( );
        attr.setKey( key );
        attr.setValue( value );
        if ( StringUtils.isNotBlank( certificationCode ) )
        {
            attr.setCertificationProcess( certificationCode );
            attr.setCertificationDate( new Date( ) );
        }
        return attr;
    }

    private RequestAuthor getAuthor( )
    {
        RequestAuthor author = new RequestAuthor( );
        author.setName( getUser( ).getEmail( ) );
        author.setType( AuthorType.application );
        return author;
    }

    private <T> T getIdentityFromCustomerId( final String customerId, final String clientCode, final Class<T> identityClass ) throws IdentityStoreException
    {
        final IdentitySearchResponse identityResponse = _identityService.getIdentity( customerId, clientCode );
        if ( identityResponse.getIdentities( ) != null && identityResponse.getIdentities( ).size( ) == 1 )
        {
            if ( identityClass.equals( QualifiedIdentity.class ) )
            {
                return identityClass.cast( identityResponse.getIdentities( ).get( 0 ) );
            }
            else
                if ( identityClass.equals( Identity.class ) )
                {
                    final QualifiedIdentity qualifiedIdentity = identityResponse.getIdentities( ).get( 0 );
                    final Identity identity = new Identity( );
                    identity.setCustomerId( qualifiedIdentity.getCustomerId( ) );
                    identity.setConnectionId( qualifiedIdentity.getConnectionId( ) );
                    identity.setAttributes( qualifiedIdentity.getAttributes( ).stream( ).map( a -> {
                        final CertifiedAttribute attribute = new CertifiedAttribute( );
                        attribute.setKey( a.getKey( ) );
                        attribute.setValue( a.getValue( ) );
                        attribute.setCertificationProcess( a.getCertifier( ) );
                        attribute.setCertificationDate( a.getCertificationDate( ) );
                        return attribute;
                    } ).collect( Collectors.toList( ) ) );
                    return identityClass.cast( identity );
                }
        }
        return null;
    }

    private void sortServiceContractAttributes( )
    {
        _serviceContract.getAttributeDefinitions( ).sort( ( a1, a2 ) -> {
            final int index1 = _sortedAttributeKeyList.indexOf( a1.getKeyName( ) );
            final int index2 = _sortedAttributeKeyList.indexOf( a2.getKeyName( ) );
            final Integer i1 = index1 == -1 ? 999 : index1;
            final Integer i2 = index2 == -1 ? 999 : index2;
            return i1.compareTo( i2 );
        } );
    }

}
