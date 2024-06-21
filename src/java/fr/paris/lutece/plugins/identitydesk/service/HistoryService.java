package fr.paris.lutece.plugins.identitydesk.service;

import fr.paris.lutece.plugins.identitydesk.business.LocalHistoryDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityServiceExtended;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;


public class HistoryService {

    private static final IdentityServiceExtended _serviceIdentity = SpringContextService.getBean("identityService.rest.httpAccess");
    private String _currentClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );

    private static HistoryService _instance;



    private HistoryService() { }

    public static HistoryService getInstance( )
    {
        if ( _instance == null )
        {
            _instance = new HistoryService( );
        }

        return _instance;
    }
    
    /**
     * Fetches the history of changes for a specific identity based on the given customerId.
     *
     * @param customerId The customerId of the identity to fetch the history for.
     * @param clientCode The client code.
     * @param author The author requesting the history.
     * @return A map containing identity history grouped by modification time.
     * @throws IdentityStoreException If there is an issue with the identity store.
     */

     public List<LocalHistoryDto> getIdentityHistory(String customerId, RequestAuthor author) throws IdentityStoreException {
        List<LocalHistoryDto> localHistoryList = new ArrayList<>();
    
        // Prepare the search request
        IdentityHistorySearchRequest request = new IdentityHistorySearchRequest();
        request.setCustomerId(customerId);
        request.setClientCode(_currentClientCode);
    
        // Execute the search request
        IdentityHistorySearchResponse response = _serviceIdentity.searchIdentityHistory(request, _currentClientCode, author);
        if (response.getStatus().getType() != ResponseStatusType.OK) {
            logAndDisplayStatusErrorMessage(response);
            return localHistoryList;
        }
    
        // Process the response histories
        for (IdentityHistory history : response.getHistories()) {
            IdentityDto identity = getQualifiedIdentityFromCustomerId(history.getCustomerId(), _currentClientCode, author);
            if (identity == null) {
                continue;
            }
    
            for (AttributeHistory attributeHistory : history.getAttributeHistories()) {
                for (AttributeChange attributeChange : attributeHistory.getAttributeChanges()) {
                    long modificationTime = Optional.ofNullable(attributeChange.getModificationDate())
                                                    .map(Date::getTime)
                                                    .orElse(0L);
                    long roundedModificationTime = (modificationTime / (1000 * 60)) * (1000 * 60);
    
                    // Find an existing LocalHistoryDto with the same modificationTime and identity
                    LocalHistoryDto existingDto = localHistoryList.stream()
                        .filter(dto -> dto.getModificationTime() == roundedModificationTime && dto.getIdentity().equals(identity))
                        .findFirst()
                        .orElse(null);
    
                    if (existingDto == null) {
                        // If no existing dto found, create a new one and add to the list
                        List<AttributeChange> attributeChanges = new ArrayList<>();
                        attributeChanges.add(attributeChange);
                        localHistoryList.add(new LocalHistoryDto(roundedModificationTime, identity, attributeChanges));
                    } else {
                        // If found, add the attributeChange to the existing dto's list
                        existingDto.getAttributeChanges().add(attributeChange);
                    }
                }
            }
        }
    
        return localHistoryList;
    }
    /**
     * get QualifiedIdentity From CustomerId
     *
     * @param customerId The customerId of the identity.
     * @param clientCode The client code.
     * @param author The author requesting the identity.
     * @return the QualifiedIdentity, null otherwise.
     * @throws IdentityStoreException If there is an issue with the identity store.
     */
    private IdentityDto getQualifiedIdentityFromCustomerId(String customerId, String clientCode, RequestAuthor author) throws IdentityStoreException {
        if (StringUtils.isNotBlank(customerId)) {
            final IdentitySearchResponse identityResponse = _serviceIdentity.getIdentityByCustomerId(customerId, clientCode, author);
            if (identityResponse.getStatus().getType() == ResponseStatusType.OK) {
                if (identityResponse.getIdentities().size() == 1) {
                    return identityResponse.getIdentities().get(0);
                }
            } else {
                logAndDisplayStatusErrorMessage(identityResponse);
            }
        }
        return null;
    }

    /**
     * Logs and displays the localized status error message if the API response is in error.
     *
     * @param apiResponse the API response.
     */
    private void logAndDisplayStatusErrorMessage(final ResponseDto apiResponse) {
        if (apiResponse != null && apiResponse.getStatus().getType() != ResponseStatusType.OK) {
            AppLogService.error(apiResponse.getStatus().getMessage());
        }
    }
    
}
