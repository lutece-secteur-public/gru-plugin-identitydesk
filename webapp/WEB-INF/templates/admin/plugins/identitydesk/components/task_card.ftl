<#--
  Macro: batchCard
  Renders a card UI for batch details.
  @param batch The main data object containing the batch details.
  @param class Optional CSS classes for customization.
  @param width Optional width for the card.
  @returns A rendered batch card based on provided parameters.
-->
<#macro taskCard task class="" width="">
    <div class="border-start border-end rounded-5 my-3 p-3 position-relative border-top border-bottom border-dark-subtle ${class}" style="<#if width!=''>width:${width}</#if>">
        <div class="row mx-0">
            <div class="col-4 pl-0">
                <div class="card p-0 rounded-5 shadow-xl my-3">
                    <div class="py-4 text-center">
                        <h3 class="px-2 text-truncate">
                            #i18n{identitydesk.display_identity_task.label.task.sectionTitle}
                        </h3>
                    </div>
                    <ul class="list-group list-group-flush rounded-bottom-5">
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.code}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.taskCode}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.type}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.taskType}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.creationDate}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.creationDate}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.lastUpdateDate}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.lastUpdateDate}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.lastUpdateClientCode}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.lastUpdateClientCode}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <div class="opacity-50">
                                        #i18n{identitydesk.display_identity_task.label.task.status}
                                    </div>
                                    <div class="fw-bold">
                                        <h3 class="mb-0 fw-bold">
                                            ${task.taskStatus}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="col-8 pr-0">
                <div class="card p-0 rounded-5 shadow-xl my-3">
                    <div class="py-4 text-center">
                        <h3 class="px-2 text-truncate">
                            #i18n{identitydesk.display_identity_task.label.task.change.historyTitle}
                        </h3>
                    </div>
                    <ul class="list-group list-group-flush rounded-bottom-5">
                        <li class="list-group-item d-flex justify-content-center align-items-center p-0 border-start-0 border-end-0" style="min-height:55px">
                            <div class="w-100 d-flex">
                                <div class="flex-1 flex-grow-1 py-2 px-3 text-break">
                                    <table class="table rounded rounded-3 overflow-hidden border table-condensed table-hover mb-0">
                                        <thead>
                                            <tr>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.date}</th>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.status}</th>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.type}</th>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.clientCode}</th>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.author.name}</th>
                                                <th>#i18n{identitydesk.display_identity_task.label.task.change.author.type}</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <#list task.taskChanges as change>
                                                <tr>
                                                    <td>${change.taskChangeDate}</td>
                                                    <td>${change.taskStatus}</td>
                                                    <td>${change.taskChangeType}</td>
                                                    <td>${change.clientCode}</td>
                                                    <td>${change.author.name}</td>
                                                    <td>${change.author.type}</td>
                                                </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </li>

                    </ul>
                </div>
            </div>
        </div>
    </div>
    <style>
        .pl-0 {
            padding-left: 0!important;
        }
        .pr-0 {
            padding-right: 0!important;
        }
    </style>
</#macro>
