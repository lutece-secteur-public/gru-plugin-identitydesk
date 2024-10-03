package fr.paris.lutece.plugins.identitydesk.service;

import fr.paris.lutece.plugins.identitydesk.business.LocalAttributeDto;
import fr.paris.lutece.plugins.identitydesk.business.LocalIdentityDto;
import fr.paris.lutece.plugins.identitydesk.dto.ExtendedIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityResourceType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.task.IdentityTaskListGetResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityServiceExtended;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

public class IdentityDeskService {
    private static final IdentityServiceExtended _serviceIdentity = SpringContextService.getBean("identitydesk.identityService");
    private final String _currentClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );

    private static IdentityDeskService _instance;

    private IdentityDeskService() { }

    public static IdentityDeskService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityDeskService( );
        }

        return _instance;
    }

    public ExtendedIdentityDto toExtendedIdentityDto(final IdentityDto identityDto, final RequestAuthor author ) {
        final ExtendedIdentityDto extendedIdentityDto = new ExtendedIdentityDto( identityDto );
        extendedIdentityDto.getTasks().addAll(this.getIdentityTasks(identityDto.getCustomerId(), author));
        return extendedIdentityDto;
    }

    public LocalIdentityDto toLocalIdentityDto( final IdentityDto identityDto, final ServiceContractDto serviceContract, final RequestAuthor author ) {
        if ( identityDto == null || serviceContract == null )
        {
            return null;
        }
        final LocalIdentityDto localIdentityDto = new LocalIdentityDto( identityDto.getCustomerId( ), identityDto.getLastUpdateDate( ) );

        // add allowed certification process from service contract
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrRef -> {
            final AttributeDto identityAttr = identityDto.getAttributes( ).stream( ).filter(a -> a.getKey( ).equals( attrRef.getKeyName( ) ) ).findFirst( )
                    .orElse( null );

            if ( CollectionUtils.isNotEmpty( attrRef.getAttributeCertifications( ) ) )
            {
                localIdentityDto.getAttributeList( ).add( LocalAttributeDto.from( identityAttr, attrRef ) );
            }
        } );

        localIdentityDto.getTasks().addAll(this.getIdentityTasks(identityDto.getCustomerId(), author));

        return localIdentityDto;
    }

    private List<IdentityTaskDto> getIdentityTasks( final String customerId, final RequestAuthor author ) {
        try
        {
            final IdentityTaskListGetResponse response = _serviceIdentity.getIdentityTaskList( customerId, IdentityResourceType.CUID.name(), _currentClientCode, author );
            return response.getTasks();
        }
        catch ( final IdentityStoreException e )
        {
            AppLogService.debug( "An error occurred trying to get the task list associated to identity " + customerId, e );
        }
        return Collections.emptyList();
    }
}
