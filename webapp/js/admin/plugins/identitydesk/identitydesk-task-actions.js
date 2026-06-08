document.addEventListener('DOMContentLoaded', function() {
    const offcanvas = document.querySelectorAll('.offcanvas');
    offcanvas.forEach((element) => {
        document.querySelector('.lutece-page').appendChild(element);
    });

    document.querySelectorAll('.identitydesk-task-action').forEach((button) => {
        button.addEventListener('click', async () => {
            if (button.disabled) {
                return;
            }
            const modalElement = document.getElementById(button.dataset.modalId);
            const offcanvasElement = document.getElementById(button.dataset.offcanvasId);
            const offcanvasBody = offcanvasElement.querySelector('.offcanvas-body');

            button.disabled = true;
            bootstrap.Modal.getOrCreateInstance(modalElement).hide();

            offcanvasBody.innerHTML = '<div class="d-flex justify-content-center align-items-center h-100 p-5"><div class="spinner-border text-primary" role="status"></div></div>';
            bootstrap.Offcanvas.getOrCreateInstance(offcanvasElement).show();

            try {
                const actionUrl = new URL(button.dataset.actionUrl, window.location.href);
                const response = await fetch(actionUrl, { credentials: 'same-origin' });
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }

                const text = await response.text();
                const doc = new DOMParser().parseFromString(text, 'text/html');
                const taskResult = doc.querySelector('#task-result');
                const newWriteTokenInput = doc.querySelector('input[name="token"]');

                if (newWriteTokenInput && newWriteTokenInput.value) {
                    document.querySelectorAll('.identitydesk-task-action').forEach((actionButton) => {
                        const nextUrl = new URL(actionButton.dataset.actionUrl, window.location.href);
                        nextUrl.searchParams.set('token', newWriteTokenInput.value);
                        actionButton.dataset.actionUrl = nextUrl.href;
                    });
                }

                offcanvasBody.innerHTML = taskResult ? taskResult.outerHTML : text;
            } catch (error) {
                console.error('IdentityDesk task action failed', error);
                offcanvasBody.innerHTML = '<div class="alert alert-danger m-4">Une erreur est survenue pendant la demande.</div>';
            } finally {
                button.disabled = false;
            }
        });
    });
});
