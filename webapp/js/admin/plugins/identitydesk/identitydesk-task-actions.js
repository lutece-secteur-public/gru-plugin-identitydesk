document.addEventListener('DOMContentLoaded', function() {
    // Les offcanvas sont déplacés au niveau de la page pour éviter les problèmes d'affichage quand ils sont déclarés dans une modale.
    const offcanvas = document.querySelectorAll('.offcanvas');
    offcanvas.forEach((element) => {
        document.querySelector('.lutece-page').appendChild(element);
    });

    // Les actions de création de compte et validation email sont déclenchées par JS pour éviter que targetUrl relance plusieurs fois l'action d'écriture.
    document.querySelectorAll('.identitydesk-task-action').forEach((button) => {
        button.addEventListener('click', async () => {
            if (button.disabled) {
                return;
            }

            // Le bouton porte les ids de la modale à fermer et de l'offcanvas qui affichera le résultat.
            const modalElement = document.getElementById(button.dataset.modalId);
            const offcanvasElement = document.getElementById(button.dataset.offcanvasId);
            const offcanvasBody = offcanvasElement.querySelector('.offcanvas-body');

            // On bloque le bouton, on ferme la modale de confirmation et on ouvre l'offcanvas avec un spinner.
            button.disabled = true;
            bootstrap.Modal.getOrCreateInstance(modalElement).hide();

            offcanvasBody.innerHTML = '<div class="d-flex justify-content-center align-items-center h-100 p-5"><div class="spinner-border text-primary" role="status"></div></div>';
            bootstrap.Offcanvas.getOrCreateInstance(offcanvasElement).show();

            try {
                // L'action d'écriture est appelée une seule fois avec le token courant.
                const actionUrl = new URL(button.dataset.actionUrl, window.location.href);
                const response = await fetch(actionUrl, { credentials: 'same-origin' });
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }

                // La réponse complète est parsée pour récupérer uniquement le bloc résultat destiné à l'offcanvas.
                const text = await response.text();
                const doc = new DOMParser().parseFromString(text, 'text/html');
                const taskResult = doc.querySelector('#task-result');
                const newWriteTokenInput = doc.querySelector('input[name="token"]');

                // createTask renvoie un nouveau token d'écriture ; on remplace l'ancien dans tous les boutons restés sur la page.
                if (newWriteTokenInput && newWriteTokenInput.value) {
                    document.querySelectorAll('.identitydesk-task-action').forEach((actionButton) => {
                        const nextUrl = new URL(actionButton.dataset.actionUrl, window.location.href);
                        nextUrl.searchParams.set('token', newWriteTokenInput.value);
                        actionButton.dataset.actionUrl = nextUrl.href;
                    });
                }

                // Si le template contient #task-result, on affiche ce bloc ; sinon on garde la réponse pour faciliter le diagnostic.
                offcanvasBody.innerHTML = taskResult ? taskResult.outerHTML : text;
            } catch (error) {
                // En cas d'erreur HTTP, token invalide ou problème JS, on garde une trace console et un message lisible côté utilisateur.
                console.error('IdentityDesk task action failed', error);
                offcanvasBody.innerHTML = '<div class="alert alert-danger m-4">Une erreur est survenue pendant la demande.</div>';
            } finally {
                // Le bouton est réactivé après traitement ; le token aura été remplacé si l'action a réussi.
                button.disabled = false;
            }
        });
    });
});
