(ns eponai.web.ui.dashboard
  (:require
    [eponai.common.report :as report]
    [eponai.web.ui.all-transactions :refer [Transaction]]
    [eponai.web.ui.d3.balance-chart :as bc]
    [eponai.web.ui.d3.pie-chart :as pc]
    [eponai.web.ui.icon :as icon]
    [om.next :as om :refer-macros [defui]]
    [sablono.core :refer-macros [html]]
    [taoensso.timbre :refer-macros [error debug]]))

(defui Dashboard
  static om/IQueryParams
  (params [_]
    {:filter {}
     :transaction (om/get-query Transaction)})
  static om/IQuery
  (query [_]
    ['({:query/transactions [:transaction/title]} {:filter ?filter})])
  Object
  (render [this]
    (let [{:keys [query/transactions]} (om/props this)
          {:keys [housing limit transport spent]} (report/summary transactions)
          balance-report (report/balance-vs-spent transactions)
          ]
      (debug "Balance: " balance-report)
      (html
        [:div#dashboard
         [:div.dashboard-section
          [:div.row.column
           [:a.button.black
            "September"
            ;[:i.fa.fa-caret-down.fa-fw]
            ]

           [:div#pie-charts
            (pc/->PieChart {:id    "housing-chart"
                            :title "Housing"
                            :value (or housing 0)
                            :limit spent})
            (pc/->PieChart {:id    "transport-chart"
                            :title "Transport"
                            :value (or transport 0)
                            :limit spent})
            (pc/->PieChart {:id    "balance-pie-chart"
                            :title "Balance"
                            :value (- limit spent)
                            :limit limit})]]]

         [:div.dashboard-section
          [:div.row.column

           [:div.section-title
            (icon/dashboard-balance)
            [:span "Balance vs Spent"]]

           [:div#balance-spent
            (bc/->BalanceChart {:id     "balance-spent-chart"
                                :values (if (seq transactions)
                                          balance-report
                                          [])})]

           [:div#key-metrics
            [:div.key-metric
             [:div.val-txt "473"]
             [:div.title-txt "Balance"]]
            [:div.key-metric
             [:div.val-txt "31.21"]
             [:div.title-txt "Avg. Spent per day"]]
            [:div.key-metric
             [:div.val-txt "53.13"]
             [:div.title-txt "By Oct 31"]]]]]

         [:div.dashboard-section
          [:div.row.column
           [:div.section-title
            (icon/dashboard-categories)
            [:span "Top Categories"]]]]]))))

(def ->Dashboard (om/factory Dashboard))
