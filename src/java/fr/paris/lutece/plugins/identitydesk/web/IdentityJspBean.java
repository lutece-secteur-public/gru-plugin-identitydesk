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
package fr.paris.lutece.plugins.identitydesk.web;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.identitydesk.business.LocalIdentityDto;
import fr.paris.lutece.plugins.identitydesk.cache.ServiceAttributeKeyReferentialCache;
import fr.paris.lutece.plugins.identitydesk.cache.ServiceContractCache;
import fr.paris.lutece.plugins.identitydesk.cache.ServiceProcessusReferentialCache;
import fr.paris.lutece.plugins.identitydesk.dto.ExtendedIdentityDto;
import fr.paris.lutece.plugins.identitydesk.rbac.AccessIdentityResource;
import fr.paris.lutece.plugins.identitydesk.service.HistoryService;
import fr.paris.lutece.plugins.identitydesk.service.IdentityDeskService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeKeyDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationLevelDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationProcessusDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityResourceType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskCreateResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitydesk/", right = "IDENTITYDESK_MANAGEMENT" )
public class IdentityJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 6053504380426222888L;
    // Templates
    private static final String TEMPLATE_SEARCH_IDENTITIES_RESULT = "/admin/plugins/identitydesk/search_identities_result.html";
    private static final String TEMPLATE_SEARCH_IDENTITIES = "/admin/plugins/identitydesk/search_identities.html";
    private static final String TEMPLATE_TASK_CREATION_RESULT = "/admin/plugins/identitydesk/view_task_creation_result.html";
    private static final String TEMPLATE_DISPLAY_IDENTITY_TASK_LIST = "/admin/plugins/identitydesk/view_identity_task_list.html";
    private static final String TEMPLATE_VIEW_IDENTITY = "/admin/plugins/identitydesk/view_identity.html";


    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitydesk/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitydesk/modify_identity.html";

    // Messages
    private static final String MESSAGE_GET_IDENTITY_ERROR = "identitydesk.message.get_identity.error";
    private static final String MESSAGE_SEARCH_IDENTITY_NORESULT_DETAIL = "identitydesk.message.search_identity.noresult.detail";
    private static final String MESSAGE_SEARCH_IDENTITY_ERROR = "identitydesk.message.search_identity.error";
    private static final String MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION = "identitydesk.message.identity.mustselectcertification";
    private static final String MESSAGE_CREATE_IDENTITY_SUCCESS = "identitydesk.message.create_identity.success";
    private static final String MESSAGE_CREATE_IDENTITY_ERROR = "identitydesk.message.create_identity.error";
    private static final String MESSAGE_UPDATE_IDENTITY_NOCHANGE = "identitydesk.message.update_identity.nochange";
    private static final String MESSAGE_UPDATE_IDENTITY_SUCCESS = "identitydesk.message.update_identity.success";
    private static final String MESSAGE_UPDATE_IDENTITY_ERROR = "identitydesk.message.update_identity.error";
    private static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitydesk.message.get_service_contract.error";
    private static final String MESSAGE_SEARCH_IDENTITY_REQUIREDFIELD = "identitydesk.message.search_identity.requiredfield";
    private static final String MESSAGE_GET_REFERENTIAL_ERROR = "referential error";
    private static final String MESSAGE_GET_IDENTITY_HISTORY_ERROR = "identitydesk.message.get_identity_history.error";
    private static final String MESSAGE_CREATE_IDENTITY_ACCOUNT_ERROR = "identitydesk.message.create_identity_account.error";
    private static final String PROPERTY_PAGE_TITLE_CREATE_TASK_IDENTITY = "identitydesk.create_identity_task.pageTitle";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitydesk.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitydesk.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitydesk.create_identity.pageTitle";
    private static final String PROPERTY_ALLOW_CLIENT_CODE_DYNAMIC_CHANGE = "identitydesk.client_code.allow_dynamic_change";
    private static final String PROPERTY_ALLOW_RETURN_URL_DYNAMIC_CHANGE = "identitydesk.return_url.allow_dynamic_change";

    // Constants
    private static final String PARAMETER_SEARCH_PREFIX = "search_";
    private static final String PARAMETER_ATTR_CERT_SUFFIX = "-certif";
    private static final String PARAMETER_APPROXIMATE = "approximate";
    private static final String PARAMETER_NEW_SEARCH = "new_search";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_ELIGIBLE_IDENTITY_TO_ACCOUNT = "eligible_identity_to_account_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_QUERY_SEARCH_ATTRIBUTES = "query_search_attributes";
    private static final String MARK_AUTOCOMPLETE_CITY_ENDPOINT = "autocomplete_city_endpoint";
    private static final String MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT = "autocomplete_country_endpoint";
    private static final String MARK_RETURN_URL = "return_url";
    private static final String MARK_ATTRIBUTE_STATUSES = "attribute_statuses";
    private static final String MARK_SEARCH_RULES = "search_rules";
    private static final String MARK_REFERENTIAL = "referential";
    private static final String APPROXIMATED_SEARCH = "approximate";
    private static final String MARK_CAN_CREATE = "can_create";
    private static final String MARK_CAN_WRITE = "can_write";
    private static final String MARK_RULES_REQ_REACHED = "rules_requirements_reached";
    private static final String MARK_ATTRIBUTE_INFO_KEY_LIST = "attribute_info_key_list";
    private static final String MARK_SEARCH_PARAMS = "search_params";
    private static final String MARK_TASK_CODE = "task_code";
    private static final String MARK_TASK_RESULT_MESSAGE = "task_result_message";
    private static final String MARK_IDENTITY_HISTORY = "history";

    // Views
    private static final String VIEW_SEARCH_IDENTITY = "searchIdentity";
    private static final String VIEW_CREATE_IDENTITY = "createIdentity";
    private static final String VIEW_MODIFY_IDENTITY = "modifyIdentity";
    private static final String VIEW_IDENTITY_TASK_LIST = "displayIdentityTaskList";
    private static final String VIEW_VIEW_IDENTITY = "viewIdentity";

    // Actions
    private static final String ACTION_SEARCH_IDENTITY = "searchIdentity";
    private static final String ACTION_CREATE_IDENTITY = "createIdentity";
    private static final String ACTION_MODIFY_IDENTITY = "modifyIdentity";
    private static final String ACTION_CREATE_ACCOUNT = "createAccount";

    // Cache
    private static final ServiceContractCache _serviceContractCache = SpringContextService.getBean( "identity.serviceContractCacheService" );
    private static final ServiceProcessusReferentialCache _serviceProcessusReferentialCache = SpringContextService
            .getBean( "identitydesk.serviceProcessusReferentialCache" );
    private static final ServiceAttributeKeyReferentialCache _serviceAttributeKeyReferentialCache = SpringContextService
            .getBean( "identitydesk.serviceAttributeKeyReferentialCache" );

    // Session variable to store working values
    private List<SearchAttribute> _searchAttributes = new ArrayList<>( );
    private ServiceContractDto _serviceContract;
    private List<AttributeCertificationProcessusDto> _processusReferential;
    private List<AttributeKeyDto> _attributesReferential;
    private List<AttributeStatus> _attributeStatuses = new ArrayList<>( );
    private String _currentClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );
    private String _returnUrl = null;

    boolean _canCreateIdentity = false;
    boolean _canWriteIdentity = false;

    private final IdentityService _identityService = SpringContextService.getBean( "identity.identityService" );
    private final String _accountCreationTaskMinLevel = AppPropertiesService.getProperty( "identitydesk.account.creation.task.eligibility.min.level" );
    private final String _autocompleteCityEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.city.endpoint" );
    private final String _autocompleteCountryEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.country.endpoint" );
    private final List<List<String>> _searchRules = AppPropertiesService.getKeys( "identitydesk.search.rule." ).stream( ).map( key -> {
        String property = AppPropertiesService.getProperty( key );
        return ( property != null && !property.trim( ).isEmpty( ) ) ? Arrays.asList( property.split( "," ) ) : new ArrayList<String>( );
    } ).collect( Collectors.toList( ) );
    private final List<String> _searchAttributeKeyStrictList = Optional
            .ofNullable( AppPropertiesService.getProperty( "identitydesk.search.strict.attributes" ) ).filter( property -> !property.trim( ).isEmpty( ) )
            .map( property -> Arrays.asList( property.split( "," ) ) ).orElseGet( ArrayList::new );
    private final List<String> _AttributeInfoKeyList = Optional.ofNullable( AppPropertiesService.getProperty( "identitydesk.attribute.information.show" ) )
            .filter( property -> !property.trim( ).isEmpty( ) ).map( property -> Arrays.asList( property.split( "," ) ) ).orElseGet( ArrayList::new );




    @View ( VIEW_VIEW_IDENTITY )
    public String getViewIdentity( HttpServletRequest request ) throws AccessDeniedException
    {
        final String customerId = request.getParameter( "customer_id" );
        final IdentityDto qualifiedIdentity;
        ExtendedIdentityDto extendedIdentity;
        IdentityHistory history = null;

        initClientCode( request );
        initServiceContract( _currentClientCode );
        initReferential( _currentClientCode );

        try
        {
            final IdentitySearchResponse getResponse = _identityService.getIdentity( customerId, _currentClientCode, getAuthor( ) );
            logAndDisplayStatusErrorMessage( getResponse );
            if ( getResponse.getIdentities( ).size( ) != 1 )
            {
                return getSearchIdentities( request );
            }
            qualifiedIdentity = getResponse.getIdentities( ).get( 0 );
            extendedIdentity = IdentityDeskService.instance().toExtendedIdentityDto( qualifiedIdentity, this.getAuthor() );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving selected identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }

        try
        {
            history = HistoryService.getInstance().getIdentityHistory(qualifiedIdentity.getCustomerId(), getAuthor());
        }
        catch (IdentityStoreException e)
        {
            AppLogService.error("Error while retrieving the history for identity [customerId = " + qualifiedIdentity.getCustomerId() + "].", e);
            addError(MESSAGE_GET_IDENTITY_HISTORY_ERROR, getLocale());
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, extendedIdentity );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_REFERENTIAL, _processusReferential );
        model.put( MARK_ATTRIBUTE_INFO_KEY_LIST, _AttributeInfoKeyList );
        model.put( MARK_CAN_WRITE, _canWriteIdentity );
        model.put( MARK_IDENTITY_HISTORY, history );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_IDENTITY ) );
        addReturnUrlMarker( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_VIEW_IDENTITY, model );
    }


    /**
     * Displays the search form
     *
     * @param request
     *            The Http request
     * @return the html code of the search form
     */
    @View( value = VIEW_SEARCH_IDENTITY, defaultView = true )
    public String getSearchIdentities( final HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !RBACService.isAuthorized( new AccessIdentityResource( ), AccessIdentityResource.PERMISSION_READ, (User) getUser( ) ) )
        {
            throw new AccessDeniedException( "You don't have the right to read identities." );
        }
        initClientCode( request );
        initServiceContract( _currentClientCode );
        initReferential( _currentClientCode );
        
        if (Boolean.parseBoolean(request.getParameter(PARAMETER_NEW_SEARCH))) {
            _searchAttributes = new ArrayList<>();
            _returnUrl = null;
        } else if (AppPropertiesService.getPropertyBoolean(PROPERTY_ALLOW_RETURN_URL_DYNAMIC_CHANGE, false)) {
            _returnUrl = StringUtils.defaultIfBlank(request.getParameter("return_url"), _returnUrl);
        }

        _canCreateIdentity = RBACService.isAuthorized( new AccessIdentityResource( ), AccessIdentityResource.PERMISSION_CREATE, (User) getUser( ) );
        final boolean rulesRequirementsReached = _searchRules.stream( )
                .anyMatch( rule -> rule.stream( ).allMatch( key -> _searchAttributes.stream( ).map( SearchAttribute::getKey ).anyMatch( key::equals ) ) );

        final Map<String, Object> model = getModel( );
        model.put( MARK_QUERY_SEARCH_ATTRIBUTES, _searchAttributes );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_SEARCH_RULES, _searchRules );
        model.put( MARK_REFERENTIAL, _processusReferential );
        model.put( MARK_CAN_CREATE, _canCreateIdentity );
        model.put( MARK_RULES_REQ_REACHED, rulesRequirementsReached );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_SEARCH_IDENTITY ) );
        addReturnUrlMarker( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
    }

    /**
     * Process the search request and returns the search results
     *
     * @param request
     *            The Http request
     * @return the html code of the search results
     */
    @Action( ACTION_SEARCH_IDENTITY )
    public String doSearchIdentities( final HttpServletRequest request ) throws AccessDeniedException
    {
        // CSRF Token control
        // if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_SEARCH_IDENTITY ) )
        // {
        // throw new AccessDeniedException( "Invalid security token" );
        // }
        return searchIdentitiesAndCreatePage( request );
    }

    @View(VIEW_IDENTITY_TASK_LIST)
    public String getDisplayIdentityTaskList( final HttpServletRequest request ) throws AccessDeniedException
    {
        // CSRF Token control
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_SEARCH_IDENTITY ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        final String customerId = request.getParameter( Constants.PARAM_ID_CUSTOMER );
        final IdentityDto qualifiedIdentity;
        try
        {
            final IdentitySearchResponse getResponse = _identityService.getIdentity( customerId, _currentClientCode, getAuthor( ) );
            logAndDisplayStatusErrorMessage( getResponse );
            if ( getResponse.getIdentities( ).size( ) != 1 )
            {
                return getSearchIdentities( request );
            }
            qualifiedIdentity = getResponse.getIdentities( ).get( 0 );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving selected identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }

        final LocalIdentityDto dto = IdentityDeskService.instance().toLocalIdentityDto( qualifiedIdentity, _serviceContract, this.getAuthor( ) );

        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, dto );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        // Ajouter les paramètres de recherche au modèle
        final Map<String, String> searchParams = collectSearchParams( );
        model.put( MARK_SEARCH_PARAMS, searchParams );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_IDENTITY ) );
        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_DISPLAY_IDENTITY_TASK_LIST, model );
    }

    @Action( ACTION_CREATE_ACCOUNT )
    public String doCreateAccount( final HttpServletRequest request ) throws AccessDeniedException
    {
        // CSRF Token control
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_SEARCH_IDENTITY ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        final String customerId = request.getParameter( Constants.PARAM_ID_CUSTOMER );
        String taskCode = null;
        String taskResultMessage = null;
        final IdentityDto qualifiedIdentity;
        try
        {
            final IdentitySearchResponse getResponse = _identityService.getIdentity( customerId, _currentClientCode, getAuthor( ) );
            logAndDisplayStatusErrorMessage( getResponse );
            if ( getResponse.getIdentities( ).size( ) != 1 )
            {
                return getSearchIdentities( request );
            }
            qualifiedIdentity = getResponse.getIdentities( ).get( 0 );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving selected identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }
        try
        {
            final IdentityTaskCreateRequest identityTaskCreateRequest = new IdentityTaskCreateRequest( );
            final IdentityTaskDto task = new IdentityTaskDto( );
            task.setTaskType( IdentityTaskType.ACCOUNT_CREATION_REQUEST.name( ) );
            task.setResourceType( IdentityResourceType.CUID.name( ) );
            task.setResourceId( customerId );
            identityTaskCreateRequest.setTask( task );
            final IdentityTaskCreateResponse identityTask = _identityService.createIdentityTask( identityTaskCreateRequest, _currentClientCode, getAuthor( ) );
            if ( identityTask.getStatus( ).getHttpCode( ) == 201 )
            {
                taskCode = identityTask.getTaskCode( );
            }
            taskResultMessage = identityTask.getStatus( ).getMessage( );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while trying to create an account for identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_CREATE_IDENTITY_ACCOUNT_ERROR, getLocale( ) );
            taskResultMessage = e.getMessage( );
        }

        final LocalIdentityDto dto = IdentityDeskService.instance().toLocalIdentityDto( qualifiedIdentity, _serviceContract, this.getAuthor() );
        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, dto );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_TASK_CODE, taskCode );
        model.put( MARK_TASK_RESULT_MESSAGE, taskResultMessage );
        // Ajouter les paramètres de recherche au modèle
        Map<String, String> searchParams = collectSearchParams( );
        model.put( MARK_SEARCH_PARAMS, searchParams );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_IDENTITY ) );
        return getPage( PROPERTY_PAGE_TITLE_CREATE_TASK_IDENTITY, TEMPLATE_TASK_CREATION_RESULT, model );
    }

    private String searchIdentitiesAndCreatePage( final HttpServletRequest request ) throws AccessDeniedException
    {
        if ( _serviceContract == null )
        {
            return getSearchIdentities( request );
        }
        final List<IdentityDto> identities = new ArrayList<>( );
        _attributeStatuses.clear( );

        // get search criterias
        collectSearchAttributes( request );
        // check
        if ( checkSearchAttributes( ) )
        {
            final String customerId = _searchAttributes.stream( ).filter( a -> a.getKey( ).equals( "customer_id" ) ).map( SearchAttribute::getValue )
                    .findFirst( ).orElse( null );
            if ( StringUtils.isNotBlank( customerId ) )
            {
                try
                {
                    final IdentitySearchResponse getResponse = _identityService.getIdentity( customerId, _currentClientCode, getAuthor( ) );
                    logAndDisplayStatusErrorMessage( getResponse );
                    if ( getResponse.getIdentities( ).size( ) == 1 )
                    {
                        identities.add( getResponse.getIdentities( ).get( 0 ) );
                    }
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.error( "Error while retrieving the identity [customerId = " + customerId + "].", e );
                    addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
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
                    final IdentitySearchResponse searchResponse = _identityService.searchIdentities( searchRequest, _currentClientCode, getAuthor( ) );
                    logAndDisplayStatusErrorMessage( searchResponse );
                    final Comparator<IdentityDto> scoringComparator = Comparator.comparingDouble( o -> o.getQuality( ).getScoring( ) );
                    identities.addAll( searchResponse.getIdentities( ).stream( ).sorted( scoringComparator.reversed( ) ).collect( Collectors.toList( ) ) );
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.error( "Error while searching identities [IdentitySearchRequest = " + searchRequest + "].", e );
                    addError( MESSAGE_SEARCH_IDENTITY_ERROR, getLocale( ) );
                }
            }
        }

        _canWriteIdentity = RBACService.isAuthorized( new AccessIdentityResource( ), AccessIdentityResource.PERMISSION_WRITE, (User) getUser( ) );
        _canCreateIdentity = RBACService.isAuthorized( new AccessIdentityResource( ), AccessIdentityResource.PERMISSION_CREATE, (User) getUser( ) );
        final boolean rulesRequirementsReached = _searchRules.stream( )
                .anyMatch( rule -> rule.stream( ).allMatch( key -> _searchAttributes.stream( ).map( SearchAttribute::getKey ).anyMatch( key::equals ) ) );

        final List<ExtendedIdentityDto> extendedIdentities = identities.stream( ).map( identityDto -> IdentityDeskService.instance().toExtendedIdentityDto( identityDto, this.getAuthor( ) ) ).collect( Collectors.toList( ) );
        final List<String> eligibleCustomerIds = extendedIdentities.stream( ).filter( this::eligibleToAccountCreation ).map( IdentityDto::getCustomerId )
                .collect( Collectors.toList( ) );
        final Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY_LIST, extendedIdentities );
        model.put( MARK_ELIGIBLE_IDENTITY_TO_ACCOUNT, eligibleCustomerIds );
        model.put( MARK_QUERY_SEARCH_ATTRIBUTES, _searchAttributes );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_SEARCH_RULES, _searchRules );
        model.put( MARK_REFERENTIAL, _processusReferential );
        model.put( MARK_CAN_CREATE, _canCreateIdentity );
        model.put( MARK_CAN_WRITE, _canWriteIdentity );
        model.put( MARK_RULES_REQ_REACHED, rulesRequirementsReached );
        model.put( APPROXIMATED_SEARCH, Boolean.parseBoolean( request.getParameter( PARAMETER_APPROXIMATE ) ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_SEARCH_IDENTITY ) );
        addReturnUrlMarker( request, model );

        if ( identities.isEmpty( ) )
        {
            return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
        }
        else
        {
            return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES_RESULT, model );
        }
    }

    /**
     * Returns the form to create a identity
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( VIEW_CREATE_IDENTITY )
    public String getCreateIdentity( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !_canCreateIdentity )
        {
            throw new AccessDeniedException( "You don't have the right to create identities." );
        }
        final IdentityDto identity = getIdentityFromRequest( request, PARAMETER_SEARCH_PREFIX, true );
        return createIdentityPage( identity, request );
    }

    /**
     * Returns the form to create an identity.
     *
     * @param request
     *            The HTTP request.
     * @param useSearchPrefix
     *            Indicates whether to use the search prefix.
     * @return The HTML code of the identity form.
     */
    public String getCreateIdentity( HttpServletRequest request, boolean useSearchPrefix )
    {
        final String parameterPrefix = useSearchPrefix ? PARAMETER_SEARCH_PREFIX : "";
        final IdentityDto identity = getIdentityFromRequest( request, parameterPrefix, true );
        return createIdentityPage( identity, request );
    }

    /**
     * Creates the identity page.
     *
     * @param identity
     *            The identity object.
     * @param request
     *            The HTTP request.
     * @return The HTML code of the identity page.
     */
    private String createIdentityPage( IdentityDto identity, HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );

        model.put( MARK_IDENTITY, IdentityDeskService.instance().toLocalIdentityDto( identity, _serviceContract, this.getAuthor() ) );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_ATTRIBUTE_STATUSES, _attributeStatuses );
        model.put( MARK_REFERENTIAL, _processusReferential );
        model.put( MARK_ATTRIBUTE_INFO_KEY_LIST, _AttributeInfoKeyList );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_IDENTITY ) );
        addReturnUrlMarker( request, model );

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
    public String doCreateIdentity( final HttpServletRequest request ) throws AccessDeniedException
    {
        // CSRF Token control
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_IDENTITY ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        _attributeStatuses.clear( );
        try
        {
            final IdentityDto identity = getIdentityFromRequest( request, "", true );
            if ( identity.getAttributes( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getCertifier( ) ) ) )
            {
                addWarning( MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION, getLocale( ) );
                return getCreateIdentity( request, false );
            }
            identityChangeRequest.setIdentity( identity );

            final IdentityChangeResponse response = _identityService.createIdentity( identityChangeRequest, _currentClientCode, this.getAuthor( ) );

            if ( response.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
            {
                logAndDisplayStatusErrorMessage( response );
                if ( response.getStatus( ).getAttributeStatuses( ) != null )
                {
                    _attributeStatuses.addAll( response.getStatus( ).getAttributeStatuses( ) );
                }
                return getCreateIdentity( request, false );
            }
            else
            {
                addInfo( MESSAGE_CREATE_IDENTITY_SUCCESS, getLocale( ) );
                _searchAttributes.add( new SearchAttribute( "customer_id", response.getCustomerId( ), AttributeTreatmentType.STRICT ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while creating the identity [IdentityChangeRequest = " + identityChangeRequest + "].", e );
            addError( MESSAGE_CREATE_IDENTITY_ERROR, getLocale( ) );
            return getCreateIdentity( request, false );
        }
        updateSearchAttributes( request );
        return searchIdentitiesAndCreatePage( request );
    }

    /**
     * Returns the form to update info about a identity
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_IDENTITY )
    public String getModifyIdentity( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !_canWriteIdentity )
        {
            throw new AccessDeniedException( "You don't have the right to modify identities." );
        }
        final String customerId = request.getParameter( "customer_id" );
        final IdentityDto qualifiedIdentity;
        try
        {
            final IdentitySearchResponse getResponse = _identityService.getIdentity( customerId, _currentClientCode, getAuthor( ) );
            logAndDisplayStatusErrorMessage( getResponse );
            if ( getResponse.getIdentities( ).size( ) != 1 )
            {
                return getSearchIdentities( request );
            }
            qualifiedIdentity = getResponse.getIdentities( ).get( 0 );
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving selected identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }
        final LocalIdentityDto dto = IdentityDeskService.instance().toLocalIdentityDto( qualifiedIdentity, _serviceContract, this.getAuthor( ) );
        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, dto );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_ATTRIBUTE_STATUSES, _attributeStatuses );
        model.put( MARK_REFERENTIAL, _processusReferential );
        model.put( MARK_ATTRIBUTE_INFO_KEY_LIST, _AttributeInfoKeyList );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_IDENTITY ) );
        addReturnUrlMarker( request, model );

        // Ajouter les paramètres de recherche au modèle
        Map<String, String> searchParams = collectSearchParams( );
        model.put( MARK_SEARCH_PARAMS, searchParams );

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
    public String doModifyIdentity( final HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_IDENTITY ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        final IdentityDto identityWithUpdates = getIdentityFromRequest( request, "", false );
        if ( identityWithUpdates.getCustomerId( ) == null )
        {
            addError( MESSAGE_UPDATE_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }

        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        _attributeStatuses.clear( );

        try
        {
            final IdentitySearchResponse getResponse = _identityService.getIdentity( identityWithUpdates.getCustomerId( ), _currentClientCode, getAuthor( ) );
            logAndDisplayStatusErrorMessage( getResponse );

            if ( getResponse.getIdentities( ).size( ) != 1 )
            {
                return getSearchIdentities( request );
            }

            final IdentityDto originalIdentity = getResponse.getIdentities( ).get( 0 );
            identityWithUpdates.getAttributes( ).removeIf( updatedAttr -> !checkIfAttributeIsModified( originalIdentity, updatedAttr ) );

            if ( CollectionUtils.isEmpty( identityWithUpdates.getAttributes( ) ) )
            {
                addInfo( MESSAGE_UPDATE_IDENTITY_NOCHANGE, getLocale( ) );
                return getModifyIdentity( request );
            }

            final List<AttributeDto> attributesWithoutCertif = identityWithUpdates.getAttributes( ).stream( )
                    .filter( a -> StringUtils.isBlank( a.getCertifier( ) ) ).collect( Collectors.toList( ) );

            if ( CollectionUtils.isNotEmpty( attributesWithoutCertif ) )
            {
                addWarning( MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION, getLocale( ) );
                return getModifyIdentity( request );
            }

            identityWithUpdates.setLastUpdateDate( originalIdentity.getLastUpdateDate( ) );
            identityChangeRequest.setIdentity( identityWithUpdates );

            final IdentityChangeResponse response = _identityService.updateIdentity( originalIdentity.getCustomerId( ), identityChangeRequest,
                    _currentClientCode, this.getAuthor( ) );

            if ( response.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
            {
                logAndDisplayStatusErrorMessage( response );

                if ( response.getStatus( ).getAttributeStatuses( ) != null )
                {
                    _attributeStatuses.addAll( response.getStatus( ).getAttributeStatuses( ) );
                }
                return getModifyIdentity( request );
            }
            else
            {
                addInfo( MESSAGE_UPDATE_IDENTITY_SUCCESS, getLocale( ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while updating the identity [IdentityChangeRequest = " + identityChangeRequest + "].", e );
            addError( MESSAGE_UPDATE_IDENTITY_ERROR, getLocale( ) );
            return getModifyIdentity( request );
        }

        updateSearchAttributes( request );
        return getModifyIdentity( request ); // Rediriger vers la page de modification
    }

    /**
     * Check if there is a modification for the attribute (value or certification process)
     * 
     * Returns true if : * attribute does not exists yet * value is updated * certification process is updated
     * 
     * false otherwise
     * 
     * @param originalIdentity
     * @param updatedAttr
     * @return true if modified
     */
    private boolean checkIfAttributeIsModified( IdentityDto originalIdentity, AttributeDto updatedAttr )
    {
        AttributeDto origAttr = originalIdentity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( updatedAttr.getKey( ) ) ).findFirst( )
                .orElse( null );

        if ( origAttr == null || !origAttr.getValue( ).equals( updatedAttr.getValue( ) )
                || ( !origAttr.getCertifier( ).equals( updatedAttr.getCertifier( ) ) && updatedAttr.getCertifier( ) != null ) )
        {
            return true;
        }

        // return false otherwise
        return false;

    }

    /**
     * collect search attributes from request
     * 
     * @param request
     * @return
     */
    private void collectSearchAttributes( final HttpServletRequest request )
    {
        final boolean strictSearch = !Boolean.parseBoolean( request.getParameter( APPROXIMATED_SEARCH ) );
        final List<SearchAttribute> searchList = _serviceContract.getAttributeDefinitions( ).stream( ).map( AttributeDefinitionDto::getKeyName )
                .map( attrKey -> {
                    switch( attrKey )
                    {
                        case Constants.PARAM_FAMILY_NAME:
                            attrKey = Constants.PARAM_COMMON_LASTNAME;
                            break;
                        case Constants.PARAM_EMAIL:
                            attrKey = Constants.PARAM_COMMON_EMAIL;
                            break;
                        default:
                            break;
                    }
                    String value = request.getParameter( PARAMETER_SEARCH_PREFIX + attrKey );
                    if ( attrKey.equals( Constants.PARAM_BIRTH_DATE ) && value != null && !value.isEmpty( ) )
                    {
                        try
                        {
                            Date date = new SimpleDateFormat( "yyyy-MM-dd" ).parse( value );
                            value = new SimpleDateFormat( "dd/MM/yyyy" ).format( date );
                        }
                        catch( ParseException e )
                        {
                            AppLogService.error( "Can't convert the date: " + value, e );
                        }
                    }
                    if ( value != null )
                    {
                        return new SearchAttribute( attrKey, value.trim( ),
                                ( _searchAttributeKeyStrictList.contains( attrKey ) || strictSearch ? AttributeTreatmentType.STRICT
                                        : AttributeTreatmentType.APPROXIMATED ) );
                    }
                    return null;
                } ).filter( searchAttribute -> searchAttribute != null && StringUtils.isNotBlank( searchAttribute.getValue( ) ) )
                .collect( Collectors.toList( ) );

        if ( searchList.isEmpty( ) )
        {
            // not a new search, keep existing criterias
            return;
        }

        _searchAttributes.clear( );
        _searchAttributes.addAll( searchList );

        if ( _searchAttributes.size( ) > 0 )
        {
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ) )
            {
                final String value = request.getParameter( PARAMETER_SEARCH_PREFIX + Constants.PARAM_BIRTH_PLACE_CODE );
                _searchAttributes.add( new SearchAttribute( Constants.PARAM_BIRTH_PLACE_CODE, value, AttributeTreatmentType.STRICT ) );
            }
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ) )
            {
                final String value = request.getParameter( PARAMETER_SEARCH_PREFIX + Constants.PARAM_BIRTH_COUNTRY_CODE );
                _searchAttributes.add( new SearchAttribute( Constants.PARAM_BIRTH_COUNTRY_CODE, value, AttributeTreatmentType.STRICT ) );
            }
        }
    }

    /**
     * collect search attributes from request
     * 
     * @param request
     * @return
     */
    private void updateSearchAttributes( final HttpServletRequest request )
    {
        final boolean strictSearch = !Boolean.parseBoolean( request.getParameter( APPROXIMATED_SEARCH ) );
        final List<String> requiredAttrs = Arrays.asList( "birthdate", "common_lastname", "first_name" );

        final List<SearchAttribute> searchList = _serviceContract.getAttributeDefinitions( ).stream( ).map( AttributeDefinitionDto::getKeyName )
                .filter( requiredAttrs::contains ) // Check if the key is in the list of required attributes
                .map( attrKey -> {
                    String value = request.getParameter( attrKey );
                    if ( attrKey.equals( "birthdate" ) && value != null && !value.isEmpty( ) )
                    {
                        try
                        {
                            Date date = new SimpleDateFormat( "yyyy-MM-dd" ).parse( value );
                            value = new SimpleDateFormat( "dd/MM/yyyy" ).format( date );
                        }
                        catch( ParseException e )
                        {
                            AppLogService.error( "Can't convert the date: " + value, e );
                        }
                    }
                    if ( value != null )
                    {
                        return new SearchAttribute( attrKey, value.trim( ),
                                ( _searchAttributeKeyStrictList.contains( attrKey ) || strictSearch ? AttributeTreatmentType.STRICT
                                        : AttributeTreatmentType.APPROXIMATED ) );
                    }
                    return null;
                } ).filter( Objects::nonNull ).filter( searchAttribute -> StringUtils.isNotBlank( searchAttribute.getValue( ) ) )
                .collect( Collectors.toList( ) );

        if ( searchList.isEmpty( ) )
        {
            // not a new search, keep existing criterias
            return;
        }

        _searchAttributes.clear( );
        _searchAttributes.addAll( searchList );
    }

    /**
     * check search criterias * email or lastname+firstname+birthdate are required
     * 
     * @return true if required values are present
     */
    private boolean checkSearchAttributes( )
    {
        final Optional<SearchAttribute> emailAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_COMMON_EMAIL ) ).findFirst( );
        if ( !emailAttr.isPresent( ) || StringUtils.isBlank( emailAttr.get( ).getValue( ) ) )
        {
            final Optional<SearchAttribute> firstnameAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_FIRST_NAME ) )
                    .findFirst( );
            final Optional<SearchAttribute> familynameAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_COMMON_LASTNAME ) )
                    .findFirst( );
            final Optional<SearchAttribute> birthdateAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) )
                    .findFirst( );
            if ( ( !firstnameAttr.isPresent( ) || StringUtils.isBlank( firstnameAttr.get( ).getValue( ) ) )
                    || ( !familynameAttr.isPresent( ) || StringUtils.isBlank( familynameAttr.get( ).getValue( ) ) )
                    || ( !birthdateAttr.isPresent( ) || StringUtils.isBlank( birthdateAttr.get( ).getValue( ) ) ) )
            {
                addInfo( MESSAGE_SEARCH_IDENTITY_REQUIREDFIELD, getLocale( ) );
                return false;
            }
        }

        return true;
    }

    /**
     * init client code * get client code from request, * or keep default client code set in properties
     * 
     * @param request
     */
    private void initClientCode( final HttpServletRequest request )
    {
        String clientCode = request.getParameter( "client_code" );
        if ( !StringUtils.isBlank( clientCode ) && AppPropertiesService.getPropertyBoolean( PROPERTY_ALLOW_CLIENT_CODE_DYNAMIC_CHANGE, false ) )
        {
            _currentClientCode = clientCode;
        }
    }

    /**
     * fill model with return url marker
     * 
     * @param request
     * @param model
     */
    private void addReturnUrlMarker( final HttpServletRequest request, final Map<String, Object> model )
    {
        if( _returnUrl != null )
        {
            model.put( MARK_RETURN_URL, _returnUrl );
        }
    }

    /**
     * get updated identity attributes from request
     * 
     * @param request
     * @return the identity with attributes to update
     */
    private IdentityDto getIdentityFromRequest( final HttpServletRequest request, String strPrefix, Boolean bCreate )
    {
        initServiceContract( _currentClientCode );
        final IdentityDto identity = new IdentityDto( );

        identity.setCustomerId( request.getParameter( Constants.PARAM_ID_CUSTOMER ) );

        // add attributes (and certification process) to identity if they are present in the request
        _serviceContract.getAttributeDefinitions( ).stream( )
                .filter( attr -> bCreate ? !StringUtils.isEmpty( request.getParameter( strPrefix + attr.getKeyName( ) ) )
                        : ( request.getParameter( strPrefix + attr.getKeyName( ) ) != null ) )
                .forEach( attr -> {
                    String attrValue = request.getParameter( strPrefix + attr.getKeyName( ) );
                    if ( "birthdate".equals( attr.getKeyName( ) ) && attrValue != null && !attrValue.isEmpty( ) )
                    {
                        try
                        {
                            Date date = new SimpleDateFormat( "yyyy-MM-dd" ).parse( attrValue );
                            attrValue = new SimpleDateFormat( "dd/MM/yyyy" ).format( date );
                        }
                        catch( ParseException e )
                        {
                            AppLogService.error( "Can't convert the date: " + attrValue, e );
                        }
                    }
                    identity.getAttributes( )
                            .add( buildAttribute( attr.getKeyName( ), attrValue, request.getParameter( attr.getKeyName( ) + PARAMETER_ATTR_CERT_SUFFIX ) ) );
                } );

        // Ensure that birthplace and birthcountry certification are the same as their code
        final AttributeDto birthplaceCodeAttr = identity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) )
                .findFirst( ).orElse( null );
        final AttributeDto birthcountryCodeAttr = identity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                .findFirst( ).orElse( null );
        identity.getAttributes( ).forEach( attr -> {
            if ( attr.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) && birthplaceCodeAttr != null
                    && StringUtils.isNotBlank( birthplaceCodeAttr.getCertifier( ) ) )
            {
                attr.setCertifier( birthplaceCodeAttr.getCertifier( ) );
                attr.setCertificationDate( birthplaceCodeAttr.getCertificationDate( ) );
            }
            if ( attr.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) && birthcountryCodeAttr != null
                    && StringUtils.isNotBlank( birthcountryCodeAttr.getCertifier( ) ) )
            {
                attr.setCertifier( birthcountryCodeAttr.getCertifier( ) );
                attr.setCertificationDate( birthcountryCodeAttr.getCertificationDate( ) );
            }
        } );
        if ( !bCreate )
        {
            identity.getAttributes( ).removeIf( a -> StringUtils.isBlank( a.getCertifier( ) ) );
        }
        return identity;
    }

    /**
     * Build attribute
     * 
     * @param key
     * @param value
     * @param certificationCode
     * @return the certifiedAttribute
     */
    private AttributeDto buildAttribute( final String key, final String value, final String certificationCode )
    {
        final AttributeDto attr = new AttributeDto( );
        attr.setKey( key );
        attr.setValue( value );
        if ( StringUtils.isNotBlank( certificationCode ) )
        {
            attr.setCertifier( certificationCode );
            attr.setCertificationDate( new Date( ) );
        }
        return attr;
    }

    /**
     * get Author
     * 
     * @return the author
     */
    private RequestAuthor getAuthor( )
    {
        RequestAuthor author = new RequestAuthor( );
        author.setName( getUser( ).getEmail( ) );
        author.setType( AuthorType.agent );
        return author;
    }

    /**
     * init service contract
     * 
     * @param clientCode
     */
    private void initServiceContract( String clientCode )
    {
        if ( _serviceContract == null )
        {
            try
            {
                _serviceContract = _serviceContractCache.get( clientCode );
            }
            catch( final IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
            }
            if ( _serviceContract == null )
            {
                addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * init referentiel
     * 
     * @param clientCode
     */
    private void initReferential( String clientCode )
    {
        if ( _processusReferential == null )
        {
            try
            {
                _processusReferential = _serviceProcessusReferentialCache.get( clientCode );
            }
            catch( final IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving processus referential [client code = " + clientCode + "].", e );
            }
            if ( _processusReferential == null )
            {
                addError( MESSAGE_GET_REFERENTIAL_ERROR, getLocale( ) );
            }
        }

        if ( _attributesReferential == null )
        {
            try
            {
                _attributesReferential = _serviceAttributeKeyReferentialCache.get( clientCode );
            }
            catch( final IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving attribute referential [client code = " + clientCode + "].", e );
            }
            if ( _attributesReferential == null )
            {
                addError( MESSAGE_GET_REFERENTIAL_ERROR, getLocale( ) );
            }
        }
    }

    /**
     * log and display in IHM the localized status message if apiResponse is in error
     * 
     * @param apiResponse
     *            the API response
     */
    private void logAndDisplayStatusErrorMessage( final ResponseDto apiResponse )
    {
        if ( apiResponse.getStatus( ).getType( ) != ResponseStatusType.OK && apiResponse.getStatus( ).getType( ) != ResponseStatusType.SUCCESS )
        {
            if ( apiResponse.getStatus( ).getType( ) == ResponseStatusType.INCOMPLETE_SUCCESS )
            {
                addWarning( apiResponse.getStatus( ).getMessageKey( ), getLocale( ) );
                AppLogService.info( apiResponse.getStatus( ).getMessage( ) );
            }
            else
            {
                addError( apiResponse.getStatus( ).getMessageKey( ), getLocale( ) );
                AppLogService.error( apiResponse.getStatus( ).getMessage( ) );
            }
            if ( apiResponse.getStatus( ).getType( ) == ResponseStatusType.NOT_FOUND )
            {
                addInfo( MESSAGE_SEARCH_IDENTITY_NORESULT_DETAIL, getLocale( ) );
            }
        }
    }

    /**
     * Collect current search parameters
     * 
     * @return
     */
    private Map<String, String> collectSearchParams( )
    {
        Map<String, String> searchParams = new HashMap<>( );
        _searchAttributes.forEach( attr -> {
            searchParams.put( PARAMETER_SEARCH_PREFIX + attr.getKey( ), attr.getValue( ) );
        } );
        return searchParams;
    }

    private boolean eligibleToAccountCreation( final ExtendedIdentityDto identityDto )
    {
        if ( identityDto.isMonParisActive( ) )
        {
            final StringBuilder error = new StringBuilder( "Computing Identity " ).append( identityDto.getCustomerId( ) )
                    .append( " eligibility to account creation :: " ).append( "Already connected" );
            AppLogService.debug( error );
            return false;
        }

        final List<String> pivotKeys = _attributesReferential.stream( ).filter( AttributeKeyDto::isPivot ).map( AttributeKeyDto::getKeyName )
                .collect( Collectors.toList( ) );
        final List<AttributeDto> pivotAttributes = identityDto.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                .collect( Collectors.toList( ) );

        if ( pivotAttributes.size( ) == pivotKeys.size( ) // Born in France
                || ( pivotAttributes.size( ) == pivotKeys.size( ) - 1 // Not born in france
                        && pivotAttributes.stream( ).map( AttributeDto::getKey ).noneMatch( s -> s.equals( Constants.PARAM_BIRTH_PLACE_CODE ) )
                        && pivotAttributes.stream( ).anyMatch( attributeDto -> attributeDto.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE )
                                && attributeDto.getValue( ).equals( "99100" ) ) ) )
        {
            final List<String> errors = new ArrayList<>( );
            final AttributeCertificationProcessusDto attributeCertificationProcessusDto = _processusReferential.stream( )
                    .filter( processus -> processus.getCode( ).equals( _accountCreationTaskMinLevel ) ).findFirst( ).orElse( null );

            for ( final AttributeDto attributeDto : pivotAttributes )
            {
                final AttributeCertificationLevelDto certificationLevel = attributeCertificationProcessusDto.getAttributeCertificationLevels( ).stream( )
                        .filter( cert -> cert.getAttributeKey( ).equals( attributeDto.getKey( ) ) ).findFirst( ).orElse( null );
                final Integer minRequiredLevel = Integer.valueOf( certificationLevel.getLevel( ).getLevel( ) );
                if ( attributeDto.getCertificationLevel( ) < minRequiredLevel )
                {
                    errors.add( "[attribute-key=" + attributeDto.getKey( ) + "][attribute-certification-level=" + attributeDto.getCertificationLevel( )
                            + "][expected-level=" + minRequiredLevel + "]" );
                }
            }
            if ( !errors.isEmpty( ) )
            {
                final StringBuilder error = new StringBuilder( "Computing Identity " + identityDto.getCustomerId( ) + " eligibility to account creation" )
                        .append( "Some errors occurred during pivot attributes validation. The minimum certification processus is " )
                        .append( _accountCreationTaskMinLevel ).append( "." );
                errors.forEach( error::append );
                AppLogService.debug( error );
            }
            return errors.isEmpty( );
        }

        // invalid
        return false;
    }
}
