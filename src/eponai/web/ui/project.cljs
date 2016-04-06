(ns eponai.web.ui.project
  (:require [eponai.web.ui.all-transactions :refer [->AllTransactions AllTransactions]]
            [eponai.web.ui.dashboard :refer [->Dashboard Dashboard]]
            [eponai.web.ui.navigation :as nav]
            [eponai.web.ui.utils :as utils]
            [eponai.client.ui :refer-macros [opts]]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [taoensso.timbre :refer-macros [debug]]))

(defn update-content [component content]
  (om/update-state! component assoc :content content))

(defn- submenu [component project]
  (html
    [:ul.menu
     [:li
      [:a.disabled
       [:i.fa.fa-user]
       [:small (count (:project/users project))]]]
     [:li
      [:a
       {:on-click #(.share component)}
       [:i.fa.fa-share-alt]]]
     [:li
      [:a
       {:on-click #(update-content component :dashboard)}
       [:span "Dashboard"]]]
     [:li
      [:a
       {:on-click #(update-content component :transactions)}
       [:span "Transactions"]]]]))

(defui Shareproject
  Object
  (render [this]
    (let [{:keys [on-close on-save]} (om/get-computed this)
          {:keys [input-email]} (om/get-state this)]
      (html
        [:div.clearfix
         [:h3 "Share"]
         [:label "Invite a friend to collaborate on this project:"]
         [:input
          {:type        "email"
           :value       (or input-email "")
           :on-change   #(om/update-state! this assoc :input-email (.. % -target -value))
           :placeholder "yourfriend@example.com"}]

         [:div.float-right
          [:a.button.secondary
           {:on-click on-close}
           "Cancel"]
          [:a.button.primary
           {:on-click #(do (on-save input-email)
                           (on-close))}
           "Share"]]]))))

(def ->Shareproject (om/factory Shareproject))

(defui project
  static om/IQuery
  (query [_]
    [{:proxy/dashboard (om/get-query Dashboard)}
     {:proxy/all-transactions (om/get-query AllTransactions)}])
  Object
  (share [this]
    (om/update-state! this assoc :share-project? true))

  (share-project [this project-uuid email]
    (om/transact! this `[(project/share ~{:project/uuid project-uuid
                                         :user/email email})]))
  (init-state [_]
    {:content :dashboard})
  (initLocalState [this]
    (.init-state this))

  (componentWillUnmount [this]
    (om/transact! this `[(ui.component.project/clear)
                         :query/transactions]))

  (render [this]
    (let [{:keys [proxy/dashboard
                  proxy/all-transactions]} (om/props this)
          {:keys [content
                  share-project?]} (om/get-state this)
          project (-> dashboard
                     :query/dashboard
                     :dashboard/project)]
      (html
        [:div
         (nav/->NavbarSubmenu (om/computed {}
                                           {:content (submenu this project)}))
         [:div#project-content
          (cond (= content :dashboard)
                (->Dashboard dashboard)

                (= content :transactions)
                (->AllTransactions all-transactions))]

         (when share-project?
           (let [on-close #(om/update-state! this assoc :share-project? false)]
             (utils/modal {:content (->Shareproject (om/computed {}
                                                                {:on-close on-close
                                                                 :on-save #(.share-project this (:project/uuid project) %)}))
                           :on-close on-close})))]))))

(def ->project (om/factory project))