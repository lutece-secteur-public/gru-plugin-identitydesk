document.addEventListener('DOMContentLoaded', () => {
    /**
     * Handle pasting into date fields.
     * @param {ClipboardEvent} event - The paste event.
     */
    const handleDatePaste = (event) => {
        event.preventDefault();
        const paste = (event.clipboardData || window.clipboardData).getData('text');
        const parts = paste.split(/[\.\-\/]/);
        let date = NaN;
        if (parts.length === 3) {
            const [first, second, third] = parts;
            date = first.length === 4 ? new Date(first, second - 1, third) :
                third.length === 4 ? new Date(third, second - 1, first) : NaN;
        }
        if (!isNaN(date)) {
            event.target.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        }
    };

    document.querySelectorAll('input[type="date"]').forEach((dateField) => {
        dateField.addEventListener('paste', handleDatePaste);
    });

    const forms = {
        modifyForm: document.getElementById('modify_identity'),
        createForm: document.getElementById('create-identity-form')
    }

    if (!forms.modifyForm && !forms.createForm) return;

    const elements = {
        birthDate: document.getElementById('birthdate'),
        birthPlace: document.querySelector('input#birthplace'),
        birthPlaceCode: document.getElementById('birthplace_code'),
        birthPlaceCertif: document.getElementById('birthplace_code-certif'),
        birthPlaceRequiredMsg: document.getElementById('birthplace_required_msg'),
        birthCountry: document.querySelector('input#birthcountry'),
        birthCountryCode: document.getElementById('birthcountry_code'),
        birthCountryCertif: document.getElementById('birthcountry_code-certif'),
        birthCountryRequiredMsg: document.getElementById('birthcountry_required_msg'),
        birthPlaceExternalContainer: document.getElementById('birthplace-external-container'),
    };
    const initialValues = {
        birthPlaceCode: elements.birthPlaceCode?.value || '',
        birthCountryCode: elements.birthCountryCode?.value || ''
    };
    /**
     * Handle the requirement of certification fields based on code fields' values.
     * @param {HTMLInputElement} codeElement - The input element for the code (birthplace or birthcountry).
     * @param {HTMLInputElement} certifElement - The certification input element.
     * @param {string} initialValue - The initial value of the code element.
     * @param {boolean} resetCertif - Whether to reset the certification field.
     */
    const handleFieldRequirement = (codeElement, certifElement, initialValue, resetCertif = true) => {
        if (resetCertif) certifElement.value = '';
        if (codeElement.value === '' && !initialValue) {
            certifElement.removeAttribute('required');
            certifElement.parentElement.querySelector('.text-danger')?.remove();
        } else {
            certifElement.setAttribute('required', true);
            const formLabel = certifElement.parentElement.querySelector('.form-label');
            if (!formLabel.querySelector('.text-danger')) {
                formLabel.insertAdjacentHTML('beforeend', '<span class="text-danger fw-bolder"><i class="ti ti-asterisk"></i></span>');
            }
        }
        if (!certifElement.value && codeElement.value) {
            certifElement.focus();
        }
    };
    /**
     * Lock (disable) specified fields.
     * @param {HTMLInputElement[]} fields - Array of fields to disable.
     */
    const lockFields = (fields) => fields.forEach((field) => field?.setAttribute('disabled', true));
    /**
     * Unlock (enable) specified fields.
     * @param {HTMLInputElement[]} fields - Array of fields to enable.
     */
    const unlockFields = (fields) => fields.forEach((field) => field?.removeAttribute('disabled'));
    /**
     * Apply rules for enabling or disabling fields based on other field values.
     */
    const applyBirthRules = () => {
        const isBirthCountryCodeFilled = elements.birthCountryCode.value || initialValues.birthCountryCode;
        const isBirthCountryCertifFilled = elements.birthCountryCertif.value;
        handleFieldRequirement(elements.birthPlaceCode, elements.birthPlaceCertif, initialValues.birthPlaceCode, false);
        handleFieldRequirement(elements.birthCountryCode, elements.birthCountryCertif, initialValues.birthCountryCode, false);
        if (!elements.birthDate.value) {
            lockFields([elements.birthPlace, elements.birthPlaceCertif, elements.birthCountry, elements.birthCountryCertif]);
            elements.birthCountryRequiredMsg.classList.remove('d-none');
            elements.birthPlaceRequiredMsg.classList.add('d-none');
        } else {
            unlockFields([elements.birthCountry, elements.birthCountryCertif]);
            elements.birthCountryRequiredMsg.classList.add('d-none');
            if (isBirthCountryCodeFilled && isBirthCountryCertifFilled) {
                unlockFields([elements.birthPlace, elements.birthPlaceCertif]);
                elements.birthPlaceRequiredMsg.classList.add('d-none');
            } else {
                lockFields([elements.birthPlace, elements.birthPlaceCertif]);
                elements.birthPlaceRequiredMsg.classList.remove('d-none');
            }
        }
    };
    /**
     * Set event listeners for fields to handle changes and apply rules.
     */
    const setFieldListeners = () => {
        elements.birthPlaceCode?.addEventListener('change', () => handleFieldRequirement(elements.birthPlaceCode, elements.birthPlaceCertif, initialValues.birthPlaceCode));
        elements.birthCountryCode?.addEventListener('change', () => {
            handleFieldRequirement(elements.birthCountryCode, elements.birthCountryCertif, initialValues.birthCountryCode);
            applyBirthRules();
            applyBirthPlaceRules();
        });
        elements.birthCountryCertif?.addEventListener('change', applyBirthRules);
        elements.birthDate?.addEventListener('change', applyBirthRules);
    };
    /**
     * Apply certification rules for fields not required in the group.
     * @param {Event} event - The change event.
     */
    const applyCertifRules = (event) => {
        const field = event.target;
        const certifElement = document.getElementById(field.name === 'birthplace' || field.name === 'birthcountry' ? `${field.name}_code-certif` : `${field.name}-certif`);
        if (field.name === 'birthplace' && initialValues.birthPlaceCode !== '') {
            return;
        }
        if (field.value) {
            certifElement.setAttribute('required', true);
            const formLabel = certifElement.parentElement.querySelector('.form-label');
            if (!formLabel.querySelector('.text-danger')) {
                formLabel.insertAdjacentHTML('beforeend', '<span class="text-danger fw-bolder"><i class="ti ti-asterisk"></i></span>');
            }
        } else {
            certifElement.removeAttribute('required');
            certifElement.parentElement.querySelector('.text-danger')?.remove();
        }
    };
    const applyBirthPlaceRules = () => {
        const birthPlaceContainer = document.getElementById('birthplace');
        if (elements.birthCountryCode.value !== "" && elements.birthCountryCode.value !== "99100") {
            elements.birthPlaceExternalContainer.classList.remove('d-none');
            elements.birthPlaceExternalContainer.querySelector('input').setAttribute('name', 'birthplace');
            birthPlaceContainer.classList.add('d-none');
            birthPlaceContainer.querySelector('input').removeAttribute('name');
            elements.birthPlaceCertif.setAttribute('name', 'birthplace-certif');
        } else {
            elements.birthPlaceExternalContainer.classList.add('d-none');
            elements.birthPlaceExternalContainer.querySelector('input').setAttribute('name', 'birthplace_external');
            birthPlaceContainer.classList.remove('d-none');
            birthPlaceContainer.querySelector('input').setAttribute('name', 'birthplace');
            elements.birthPlaceCertif.setAttribute('name', 'birthplace_code-certif');
        }
    };
    document.querySelectorAll('.identitydesk-js-rules input, .identitydesk-js-rules select').forEach((field) => {
        field.addEventListener('change', applyCertifRules);
    });
    applyBirthRules();
    setFieldListeners();
    applyBirthPlaceRules();


    if (forms.modifyForm) {

        const modifyElements = {
            summaryEmptyModal: document.getElementById('summaryEmptyModal'),
            summaryTableModal: document.getElementById('summaryTableModal'),
            summaryModal: document.getElementById('summaryModal'),
            changesList: document.getElementById('changesList'),
            confirmSubmit: document.getElementById('confirmSubmit'),
            allAttrs: document.querySelectorAll('.identitydesk-attribute input, .identitydesk-attribute select')
        }


        const summaryModal = new bootstrap.Modal(modifyElements.summaryModal, {
            backdrop: 'static',
            keyboard: false
        });
        let initialAttrValues = {};
        let initialCertifValues = {};
        modifyElements.allAttrs.forEach((element) => {
            initialAttrValues[element.name] = element.value;
            const certifElement = document.getElementById(element.name === 'birthplace' || element.name === 'birthcountry' ? `${element.name}_code-certif` : `${element.name}-certif`);
            if (certifElement) {
                initialCertifValues[certifElement.name] = certifElement.value;
            }
        });
        /**
         * Get the changes made to the form fields.
         * @returns {Array} - Array of changes.
         */
        const getChanges = () => {
            const changes = [];
            modifyElements.allAttrs.forEach((element) => {
                if (element.offsetParent !== null) {
                    const label = element.closest('.identitydesk-attribute').querySelector('.form-label');
                    const labelText = label ? label.textContent.trim() : element.name;
                    const certifElement = document.getElementById(element.name === 'birthplace' || element.name === 'birthcountry' ? `${element.name}_code-certif` : `${element.name}-certif`);
                    const initialCertifValue = certifElement ? initialCertifValues[certifElement.name] : '';
                    const finalCertifValue = certifElement ? certifElement.value : '';
                    if (element.value !== initialAttrValues[element.name] || finalCertifValue !== initialCertifValue) {
                        changes.push({
                            field: labelText,
                            oldValue: initialAttrValues[element.name],
                            newValue: element.value,
                            initialCertifValue: initialCertifValue,
                            finalCertifValue: finalCertifValue
                        });
                    }
                }
            });
            return changes;
        };
        /**
         * Create a table cell with content and condition.
         * @param {*} content 
         * @param {*} condition 
         * @returns 
         */
        function createSummaryTableCell(content, condition = false) {
            return `<td class="${condition ? 'text-danger fw-bolder' : ''} text-nowrap">${content ? content : '<i class="ti ti-x fs-2"></i>'}</td>`;
        }
        /**
         * Show the summary modal with changes.
         * @param {Array} changes - Array of changes.
         */
        const showSummaryModal = (changes) => {
            modifyElements.changesList.innerHTML = '';
            changes.forEach((change) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                ${createSummaryTableCell(change.field)}
                ${createSummaryTableCell(change.oldValue)}
                ${createSummaryTableCell(change.newValue, change.oldValue !== change.newValue)}
                ${createSummaryTableCell(change.initialCertifValue)}
                ${createSummaryTableCell(change.finalCertifValue, change.initialCertifValue !== change.finalCertifValue)}
            `;
                modifyElements.changesList.appendChild(tr);
            });
            modifyElements.summaryEmptyModal.classList.add('d-none');
            modifyElements.summaryTableModal.classList.remove('d-none');
            summaryModal.show();
        };
        /**
         * Handle the submission of the form.
         */
        forms.modifyForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const changes = getChanges();
            if (changes.length > 0) {
                showSummaryModal(changes);
            } else {
                modifyElements.summaryEmptyModal.classList.remove('d-none');
                modifyElements.summaryTableModal.classList.add('d-none');
                summaryModal.show();
            }
        });
        modifyElements.confirmSubmit.addEventListener('click', () => {
            summaryModal.hide();
            forms.modifyForm.submit();
        });
    }
});
