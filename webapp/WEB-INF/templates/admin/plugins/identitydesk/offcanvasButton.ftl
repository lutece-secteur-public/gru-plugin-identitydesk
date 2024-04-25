<#--
Macro: offcanvasButton

Description: Activate the offcanvasDesk set in the idcanva.

Parameters:
- id (string, required): the ID of the button component.
- idcanvas (string, required): the ID of the off-canvas component.
- buttonIcon (string, optional): the name of the icon
- title (string, optional): the title of the off-canvas component.
-->
<#macro offcanvasButton id idcanvas buttonIcon='' title=''>
    <a id="${id}" class="btn btn-primary btn-sm"
       data-bs-toggle="offcanvas" data-bs-scroll=false role="button"
       aria-controls="${idcanvas}" data-bs-backdrop="true" href="#${idcanvas}"
        <#if title!=''>
            title="${title}"
        </#if>
        >
        <#if buttonIcon!=''>
            <#local buttonIcon = buttonIcon />
            <@icon style=buttonIcon />
        </#if></a>
</#macro>