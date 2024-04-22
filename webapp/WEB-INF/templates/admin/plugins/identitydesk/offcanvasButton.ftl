<#--
Macro: offcanvasButton

Description: Activate the offcanvasDesk set in the idcanva.

Parameters:
- id (string, required): the ID of the button component.
- idcanvas (string, required): the ID of the off-canvas component.
- title (string, required): the title of the off-canvas component.
-->
<#macro offcanvasButton id idcanvas title>
    <a id="${id}" class="btn btn-primary btn-sm"
       data-bs-toggle="offcanvas" data-bs-scroll=false role="button"
       aria-controls="${idcanvas}" data-bs-backdrop="true" href="#${idcanvas}"
    >${title}</a>
</#macro>