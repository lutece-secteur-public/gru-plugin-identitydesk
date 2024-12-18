package fr.paris.lutece.plugins.identitydesk.service;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistoryGetResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityServiceExtended;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class HistoryService {

    private static final IdentityServiceExtended _serviceIdentity = SpringContextService.getBean("identitydesk.identityService");
    private final String _currentClientCode = AppPropertiesService.getProperty("identitydesk.default.client.code");

    private static HistoryService _instance;

    private HistoryService() {
    }

    public static HistoryService getInstance() {
        if (_instance == null) {
            _instance = new HistoryService();
        }

        return _instance;
    }

    /**
     * Fetches the history of changes for a specific identity based on the given
     * customerId.
     *
     * @param customerId The customerId of the identity to fetch the history for.
     * @param author     The author requesting the history.
     * @return the history
     * @throws IdentityStoreException If there is an issue with the identity store.
     */
    public IdentityHistory getIdentityHistory(String customerId, RequestAuthor author)
            throws IdentityStoreException {
        IdentityHistoryGetResponse response = _serviceIdentity.getIdentityHistory(customerId, _currentClientCode,
                author);
        if (response.getStatus().getType() != ResponseStatusType.OK) {
            logAndDisplayStatusErrorMessage(response);
            return null;
        }
        return response.getHistory();
    }

    /**
     * Logs and displays the localized status error message if the API response is
     * in error.
     *
     * @param apiResponse the API response.
     */
    private void logAndDisplayStatusErrorMessage(final ResponseDto apiResponse) {
        if (apiResponse != null && apiResponse.getStatus().getType() != ResponseStatusType.OK) {
            AppLogService.error(apiResponse.getStatus().getMessage());
        }
    }

}
