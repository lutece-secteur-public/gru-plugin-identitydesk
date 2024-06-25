export default class IdentityDeskBack {
    constructor(url) {
        this.url = url;
        this.table = document.querySelector('table');
        this.redirectButtonContainer = document.getElementById('redirectButtonContainer');
        this.redirectButton = document.getElementById('redirectButton');
        this.selectedCountElement = document.getElementById('selectedCount');
        this.cancelButton = document.getElementById('cancelButton');
        this.selectedCount = 0;
        this.selectedValues = [];

        this.init();
    }

    init() {
        this.updateCheckboxes();
        this.updateRedirectButton();
        this.attachEventListeners();
    }

    updateCheckboxes() {
        const checkboxes = this.table.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            if (!checkbox.checked) {
                checkbox.disabled = this.selectedCount >= 2;
            }
        });
    }

    updateRedirectButton() {
        this.selectedCountElement.textContent = this.selectedCount;
        
        if (this.selectedCount > 0) {
            const params = new URLSearchParams();
            this.selectedValues.forEach((value, index) => {
                params.append(`cuid_${index + 1}`, value);
            });
            this.redirectButton.href = `${this.url}?${params.toString()}`;
            this.redirectButtonContainer.classList.remove('d-none');
            
            if (!this.redirectButtonContainer.classList.contains('fade-in')) {
                this.redirectButtonContainer.classList.add('fade-in');
            }
        } else {
            this.redirectButtonContainer.classList.add('d-none');
            this.redirectButtonContainer.classList.remove('fade-in');
        }
    }

    handleCheckboxChange(e) {
        if (e.target.type === 'checkbox') {
            const row = e.target.closest('tr');
            
            if (e.target.checked) {
                row.querySelectorAll('td').forEach(cell => {
                    cell.classList.add('bg-primary-subtle');
                });
                this.selectedCount++;
                this.selectedValues.push(e.target.value);
            } else {
                row.querySelectorAll('td').forEach(cell => {
                    cell.classList.remove('bg-primary-subtle');
                });
                this.selectedCount--;
                this.selectedValues = this.selectedValues.filter(value => value !== e.target.value);
            }

            this.updateCheckboxes();
            this.updateRedirectButton();
        }
    }

    handleCancelButton(e) {
        e.preventDefault(); // Prevent the default link behavior

        const checkboxes = this.table.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
            checkbox.disabled = false;
            const row = checkbox.closest('tr');
            row.querySelectorAll('td').forEach(cell => {
                cell.classList.remove('bg-primary-subtle');
            });
        });

        this.selectedCount = 0;
        this.selectedValues = [];
        this.updateRedirectButton();
    }

    attachEventListeners() {
        this.table.addEventListener('change', this.handleCheckboxChange.bind(this));
        this.cancelButton.addEventListener('click', this.handleCancelButton.bind(this));
    }
}