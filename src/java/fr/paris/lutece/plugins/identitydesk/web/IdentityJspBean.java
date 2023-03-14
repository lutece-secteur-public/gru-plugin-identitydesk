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
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitydesk/", right = "IDENTITYDESK_MANAGEMENT" )
public class IdentityJspBean extends ManageIdentitiesJspBean
{
    /**
     *
     */
    private static final long serialVersionUID = 6053504380426222888L;
    // Templates
    private static final String TEMPLATE_MANAGE_IDENTITIES = "/admin/plugins/identitydesk/manage_identities.html";
    private static final String TEMPLATE_SEARCH_IDENTITIES = "/admin/plugins/identitydesk/search_identities.html";
    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitydesk/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitydesk/modify_identity.html";
    private static final String TEMPLATE_VIEW_IDENTITY = "/admin/plugins/identitydesk/view_identity.html";
    private static final String TEMPLATE_VIEW_ATTRIBUTE_HISTORY = "/admin/plugins/identitydesk/view_attribute_change_history.html";

    // Parameters
    private static final String PARAMETER_ID_IDENTITY = "id";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_FAMILY_NAME = "family_name";
    private static final String PARAMETER_QUERY = "query";
    private static final String PARAMETER_QUERY_FILTER = "query_filter";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitydesk.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitydesk.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitydesk.create_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY = "identitydesk.view_change_history.pageTitle";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_ATTRIBUTES_CHANGE_MAP = "attributes_change_map";
    private static final String MARK_ATTRIBUTES_CURRENT_MAP = "attributes_current_map";
    private static final String MARK_CERTIFIERS_MAP = "certifiers_map";
    private static final String MARK_QUERY = "query";
    private static final String MARK_QUERY_FILTER = "query_filter";
    private static final String MARK_QUERY_FILTER_REFLIST = "query_filter_list";
    private static final String MARK_QUERY_SEARCH_ATTRIBUTES = "query_search_attributes";
    private static final String MARK_HAS_CREATE_ROLE = "createIdentityRole";
    private static final String MARK_HAS_MODIFY_ROLE = "modifyIdentityRole";
    private static final String MARK_HAS_DELETE_ROLE = "deleteIdentityRole";
    private static final String MARK_HAS_VIEW_ROLE = "viewIdentityRole";
    private static final String MARK_HAS_ATTRIBUTS_HISTO_ROLE = "histoAttributsRole";
    private static final String JSP_MANAGE_IDENTITIES = "jsp/admin/plugins/identitydesk/ManageIdentities.jsp";
    private static final String MARK_AUTOCOMPLETE_CITY_ENDPOINT = "autocomplete_city_endpoint";
    private static final String MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT = "autocomplete_country_endpoint";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_IDENTITY = "identitydesk.message.confirmRemoveIdentity";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitydesk.model.entity.identity.attribute.";

    // Views
    private static final String VIEW_MANAGE_IDENTITIES = "manageIdentitys";
    private static final String VIEW_CREATE_IDENTITY = "createIdentity";
    private static final String VIEW_MODIFY_IDENTITY = "modifyIdentity";
    private static final String VIEW_IDENTITY = "viewIdentity";
    private static final String VIEW_ATTRIBUTE_HISTORY = "viewAttributeHistory";

    // Actions
    private static final String ACTION_CREATE_IDENTITY = "createIdentity";
    private static final String ACTION_MODIFY_IDENTITY = "modifyIdentity";
    private static final String ACTION_REMOVE_IDENTITY = "removeIdentity";
    private static final String ACTION_CONFIRM_REMOVE_IDENTITY = "confirmRemoveIdentity";

    // Infos
    private static final String INFO_IDENTITY_CREATED = "identitydesk.info.identity.created";
    private static final String INFO_IDENTITY_UPDATED = "identitydesk.info.identity.updated";
    private static final String INFO_IDENTITY_REMOVED = "identitydesk.info.identity.removed";

    // Session variable to store working values
    private Identity _identity;
    private String _strQuery;
    private String _strQueryFilter;
    private List<SearchAttributeDto> _searchAttributes = new ArrayList<>( );
    private ServiceContractDto _serviceContract;

    private IdentityService _identityService = SpringContextService.getBean( "identityService.rest.httpAccess" );

    private final String _autocompleteCityEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.city.endpoint" );
    private final String _autocompleteCountryEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.country.endpoint" );

    @View( value = VIEW_MANAGE_IDENTITIES, defaultView = true )
    public String getManageIdentitys( HttpServletRequest request )
    {
        final List<QualifiedIdentity> qualifiedIdentities = new ArrayList<>( );

        if ( collectSearchAttributes( request ) && CollectionUtils.isNotEmpty( _searchAttributes ) )
        {
            final IdentitySearchRequest searchRequest = new IdentitySearchRequest( );
            final SearchDto search = new SearchDto( );
            searchRequest.setSearch( search );
            search.setAttributes( _searchAttributes );
            try
            {
                final IdentitySearchResponse searchResponse = _identityService.searchIdentities( searchRequest, "TEST" );
                qualifiedIdentities.addAll( searchResponse.getIdentities( ) );
                if ( qualifiedIdentities.isEmpty( ) )
                {
                    addWarning( "Aucun résultat pour votre recherche." );
                }
            }
            catch( IdentityStoreException e )
            {
                e.printStackTrace( ); // FIXME logger ?
                addError( "Une erreur est survenue pendant la recherche d'identités." );
            }
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, qualifiedIdentities, JSP_MANAGE_IDENTITIES );
        model.put( MARK_QUERY, _strQuery );
        model.put( MARK_QUERY_FILTER, _strQueryFilter );
        model.put( MARK_QUERY_SEARCH_ATTRIBUTES, _searchAttributes );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
    }

    private boolean collectSearchAttributes( final HttpServletRequest request )
    {
        final String email_login = request.getParameter( "email_login" );
        final String gender = request.getParameter( "gender" );
        final String family_name = request.getParameter( "family_name" );
        final String preferred_username = request.getParameter( "preferred_username" );
        final String first_name = request.getParameter( "first_name" );
        final String birthdate = request.getParameter( "birthdate" );
        final String insee_birthplace_label = request.getParameter( "insee_birthplace_label" );
        final String insee_birthplace_code = request.getParameter( "insee_birthplace_code" );
        final String insee_birthcountry_label = request.getParameter( "insee_birthcountry_label" );
        final String insee_birthcountry_code = request.getParameter( "insee_birthcountry_code" );
        final String phone = request.getParameter( "mobile_phone" );

        final List<QualifiedIdentity> qualifiedIdentities = new ArrayList<>( );
        if ( !Arrays
                .asList( email_login, gender, family_name, preferred_username, first_name, birthdate, insee_birthplace_label, insee_birthcountry_label, phone )
                .contains( null ) )
        {
            _searchAttributes = new ArrayList<>( );
            if ( StringUtils.isNotBlank( email_login ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "email_login", email_login, true ) );
                _searchAttributes.add( new SearchAttributeDto( "email", email_login, true ) );
            }
            if ( StringUtils.isNotBlank( gender ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "gender", gender, true ) );
            }
            if ( StringUtils.isNotBlank( family_name ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "family_name", family_name, false ) );
            }
            if ( StringUtils.isNotBlank( preferred_username ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "preferred_username", preferred_username, false ) );
            }
            if ( StringUtils.isNotBlank( first_name ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "first_name", first_name, false ) );
            }
            if ( StringUtils.isNotBlank( birthdate ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "birthdate", birthdate, true ) );
            }
            if ( StringUtils.isNotBlank( insee_birthplace_label ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "insee_birthplace_label", insee_birthplace_label, true ) );
            }
            if ( StringUtils.isNotBlank( insee_birthplace_code ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "insee_birthplace_code", insee_birthplace_code, true ) );
            }
            if ( StringUtils.isNotBlank( insee_birthcountry_label ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "insee_birthcountry_label", insee_birthcountry_label, true ) );
            }
            if ( StringUtils.isNotBlank( insee_birthcountry_code ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "insee_birthcountry_code", insee_birthcountry_code, true ) );
            }
            if ( StringUtils.isNotBlank( phone ) )
            {
                _searchAttributes.add( new SearchAttributeDto( "mobile_phone", phone, true ) );
                _searchAttributes.add( new SearchAttributeDto( "fixed_phone", phone, true ) );
            }
            if ( StringUtils.isBlank( email_login ) && ( StringUtils.isAnyBlank( first_name, family_name, birthdate ) ) )
            {
                addInfo( "Pour lancer une recherche, merci de renseigner au minimum soit :"
                        + "<ul><li>Le critère Email</li><li>Les critères Prénoms + Nom de naissance + Date de naissance</li></ul>" );
                return false;
            }
            return true;
        }
        return false;
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
        _identity = initNewIdentity( request );
        try
        {
            final ServiceContractSearchResponse response = _identityService.getServiceContract( "TEST" );
            _serviceContract = response.getServiceContract( );
        }
        catch( IdentityStoreException e )
        {
            e.printStackTrace( );
            addError( "Erreur lors de la récupération du contrat de service" );
            return getManageIdentitys( request );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_CREATE_IDENTITY, model );
    }

    private Identity initNewIdentity( final HttpServletRequest request )
    {
        final Identity identity = new Identity( );
        request.getParameterMap( ).entrySet( ).stream( )
                .filter( entry -> !entry.getKey( ).startsWith( "action_" ) && !entry.getKey( ).startsWith( "view_" ) && !entry.getKey( ).endsWith( "-certif" )
                        && !entry.getKey( ).startsWith( "insee_" ) && !entry.getKey( ).equals( "customer_id" ) )
                .filter( entry -> entry.getValue( ) != null && entry.getValue( ).length == 1 && StringUtils.isNotBlank( entry.getValue( ) [0] ) )
                .forEach( entry -> identity.getAttributes( )
                        .add( initAttribute( entry.getKey( ), entry.getValue( ) [0], request.getParameter( entry.getKey( ) + "-certif" ) ) ) );

        final String insee_birthplace_label = request.getParameter( "insee_birthplace_label" );
        final String insee_birthplace_code = request.getParameter( "insee_birthplace_code" );
        final String insee_birthcountry_label = request.getParameter( "insee_birthcountry_label" );
        final String insee_birthcountry_code = request.getParameter( "insee_birthcountry_code" );

        if ( StringUtils.isNotBlank( insee_birthplace_label ) )
        {
            identity.getAttributes( )
                    .add( initAttribute( "insee_birthplace_label", insee_birthplace_label, request.getParameter( "insee_birthplace_label-certif" ) ) );
            identity.getAttributes( )
                    .add( initAttribute( "insee_birthplace_code", insee_birthplace_code, request.getParameter( "insee_birthplace_label-certif" ) ) );
        }
        if ( StringUtils.isNotBlank( insee_birthcountry_label ) )
        {
            identity.getAttributes( )
                    .add( initAttribute( "insee_birthcountry_label", insee_birthcountry_label, request.getParameter( "insee_birthcountry_label-certif" ) ) );
            identity.getAttributes( )
                    .add( initAttribute( "insee_birthcountry_code", insee_birthcountry_code, request.getParameter( "insee_birthcountry_label-certif" ) ) );
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
        try
        {
            final Identity identity = initNewIdentity( request );
            final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
            identityChangeRequest.setIdentity( identity );
            identityChangeRequest.setOrigin( this.getAuthor( ) );

            final IdentityChangeResponse response = _identityService.createIdentity( identityChangeRequest, "TEST" );
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
            }
        }
        catch( final IdentityStoreException e )
        {
            e.printStackTrace( );
            addError( e.getMessage( ) );
            throw new RuntimeException( e );
        }
        return getManageIdentitys( request );
    }

    /**
     * Gives the author
     *
     * @return the author
     */
    private RequestAuthor getAuthor( )
    {
        RequestAuthor author = new RequestAuthor( );
        author.setName( getUser( ).getEmail( ) );
        author.setType( AuthorType.application );
        return author;
    }

    /**
     * Manages the removal form of a identity whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_IDENTITY )
    public String getConfirmRemoveIdentity( HttpServletRequest request )
    {
        // if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) )
        // {
        // return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        // }
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_IDENTITY ) );
        url.addParameter( PARAMETER_ID_IDENTITY, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_IDENTITY, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a identity
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage identitys
     */
    @Action( ACTION_REMOVE_IDENTITY )
    public String doRemoveIdentity( HttpServletRequest request )
    {
        // if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) )
        // {
        // return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        // }
        // int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        // Identity identity = IdentityHome.findByPrimaryKey( nId );
        // IdentityHome.hardRemove( nId );
        // addInfo( INFO_IDENTITY_REMOVED, getLocale( ) );
        //
        // // notify listeners
        // IdentityChange identityChange = new IdentityChange( );
        // identityChange.setIdentity( identity );
        // identityChange.setChangeType( IdentityChangeType.DELETE );
        // TODO voir compat IdentityStoreNotifyListenerService.instance( ).notifyListenersIdentityChange( identityChange );

        return redirectView( request, VIEW_MANAGE_IDENTITIES );
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
        try
        {
            _identity = this.getIdentityFromCustomerId( customerId, "TEST" );
            if ( _identity == null )
            {
                addError( "Erreur lors de la récupération de l'identité sélectionnée" );
                return getManageIdentitys( request );
            }
            final ServiceContractSearchResponse contractResponse = _identityService.getServiceContract( "TEST" );
            _serviceContract = contractResponse.getServiceContract( );
        }
        catch( IdentityStoreException e )
        {
            addError( "Erreur lors de la récupération de l'identité sélectionnée" );
            return getManageIdentitys( request );
        }
        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_IDENTITY, TEMPLATE_MODIFY_IDENTITY, model );
    }

    private Identity getIdentityFromCustomerId( final String customerId, final String applicationCode ) throws IdentityStoreException
    {
        final IdentitySearchResponse identityResponse = _identityService.getIdentityByCustomerId( customerId, "TEST" );
        Identity identity = null;
        if ( identityResponse.getIdentities( ) != null && identityResponse.getIdentities( ).size( ) == 1 )
        {
            final QualifiedIdentity qualifiedIdentity = identityResponse.getIdentities( ).get( 0 );
            identity = new Identity( );
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
        }
        return identity;
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
        try
        {
            final Identity identityToUpdate = this.getIdentityFromCustomerId( customerId, "TEST" );
            if ( identityToUpdate == null )
            {
                addError( "Erreur lors de la récupération de l'identité sélectionnée" );
                return getManageIdentitys( request );
            }
            final IdentityChangeRequest changeRequest = new IdentityChangeRequest( );
            final Identity identityFromParams = this.initNewIdentity( request );

            final List<CertifiedAttribute> updatedAttributes = identityFromParams.getAttributes( ).stream( ).map( newAttr -> {
                final CertifiedAttribute oldAttr = identityToUpdate.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( newAttr.getKey( ) ) )
                        .findFirst( ).orElse( null );
                if ( oldAttr != null && oldAttr.getValue( ).equals( newAttr.getValue( ) )
                        && Objects.equals( oldAttr.getCertificationProcess( ), newAttr.getCertificationProcess( ) ) )
                {
                    return oldAttr;
                }
                return newAttr;
            } ).collect( Collectors.toList( ) );
            updatedAttributes.addAll( identityToUpdate.getAttributes( ).stream( )
                    .filter( oldAttr -> identityFromParams.getAttributes( ).stream( ).noneMatch( newAttr -> newAttr.getKey( ).equals( oldAttr.getKey( ) ) ) )
                    .collect( Collectors.toList( ) ) );

            identityToUpdate.setAttributes( updatedAttributes );
            changeRequest.setIdentity( identityToUpdate );
            changeRequest.setOrigin( this.getAuthor( ) );
            final IdentityChangeResponse response = _identityService.updateIdentity( changeRequest, "TEST" );
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
            }
        }
        catch( IdentityStoreException e )
        {
            e.printStackTrace( );
            addError( e.getMessage( ) );
            throw new RuntimeException( e );
        }
        return getManageIdentitys( request );
    }

    /**
     * view identity
     *
     * @param request
     *            http request
     * @return The HTML form to view info
     */
    @View( VIEW_IDENTITY )
    public String getViewIdentity( HttpServletRequest request )
    {
        // int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        final String nId = request.getParameter( PARAMETER_ID_IDENTITY );

        // _identity = IdentityHome.findByCustomerId( nId );
        //
        Map<String, Object> model = getModel( );
        // model.put( MARK_IDENTITY, _identity );
        // model.put( MARK_HAS_ATTRIBUTS_HISTO_ROLE,
        // IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_ATTRIBUTS_HISTO, getUser( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_VIEW_IDENTITY, model );
    }

    /**
     * Build the attribute history View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_ATTRIBUTE_HISTORY )
    public String getAttributeHistoryView( HttpServletRequest request )
    {
        // here we use a LinkedHashMap to have same attributs order as in viewIdentity
        final Map<String, List<AttributeChange>> mapAttributesChange = new LinkedHashMap<>( );

        // if ( _identity != null && MapUtils.isNotEmpty( _identity.getAttributes( ) ) )
        // {
        // for ( String strAttributeKey : _identity.getAttributes( ).keySet( ) )
        // {
        // mapAttributesChange.put( strAttributeKey, IdentityAttributeHome.getAttributeChangeHistory( _identity.getId( ), strAttributeKey ) );
        // }
        // }

        final Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTES_CHANGE_MAP, mapAttributesChange );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY, TEMPLATE_VIEW_ATTRIBUTE_HISTORY, model );
    }
}
