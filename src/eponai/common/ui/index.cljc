(ns eponai.common.ui.index
  (:require
    [eponai.common.ui.common :as common]
    [eponai.common.ui.navbar :as nav]
    [om.dom :as dom]
    [om.next :as om :refer [defui]]))

(defn top-feature [opts icon title text]
  (dom/div #js {:className "feature-item column"}
    (dom/div #js {:className "row"}
      (dom/div #js {:className "column align-self-middle small-2 medium-12"}
        (dom/i #js {:className (str "fa fa-fw " icon)}))
      (dom/div #js {:className "text-content columns"}
        (dom/strong #js {:className "feature-title"} title)
        (dom/p nil text)))))

(defn banner [{:keys [color align] :as opts} & content]
  (let [align (or align :left)
        color (or color :default)]
    (dom/div #js {:className (str "banner " (name color))}
      (dom/div #js {:className "row"}
        (apply dom/div #js {:className (str "column small-12 medium-8"
                                        (when (= (name align) "right")
                                          " medium-offset-4 text-right"))}
               content)))))

(defn content-section [{:keys [href]} header content footer]
  (dom/div #js {:className "row column section text-center"}
    (dom/div #js {:className "row column"}
      (dom/h3 #js {:className "text-center section-header"} header))
    (apply dom/div #js {:className "row small-up-2 medium-up-4"}
           content)
    (dom/div #js {:className "row column section-footer"}
      (dom/a #js {:href      href} footer))))

(defn link-to-store [store]
  (str "/store/" (:db/id store)))

(defn online-channel-element [channel]
  (let [{:stream/keys [store viewer-count img-src]
         stream-name :stream/name} channel
        store-link (link-to-store store)]
    (dom/div #js {:className "column content-item online-channel"}
      (dom/a #js {:href store-link}
             (dom/div #js {:className "photo-container"}
               (dom/div #js {:className "photo square" :style #js {:backgroundImage (str "url(" img-src ")")}})))
      (dom/div #js {:className "content-item-title-section"}
        (dom/a #js {:href store-link} (dom/strong nil stream-name))
        (common/viewer-element viewer-count))
      (dom/div #js {:className "content-item-subtitle-section"}
        (dom/a #js {:href store-link} (:store/name store))))))

(defn store-element [store]
  (let [[large mini-1 mini-2] (:store/featured-img-src store)
        store-link (link-to-store store)]
    (dom/div #js {:className "column content-item store-item"}
      (dom/a #js {:href store-link}
             (dom/div #js {:className "photo-container collage"}
               (dom/div #js {:className "photo square"
                         :style     #js {:backgroundImage (str "url(" large ")")}})
               (dom/div #js {:className "mini-container"}
                 (dom/div #js {:className "photo"
                           :style     #js {:backgroundImage (str "url(" mini-1 ")")}})
                 (dom/div #js {:className "photo"
                           :style     #js {:backgroundImage (str "url(" mini-2 ")")}}))))
      (dom/div #js {:className "content-item-title-section"}
        (dom/a #js {:href store-link}
               (:store/name store)))
      (dom/div #js {:className "content-item-subtitle-section"}
        (common/rating-element (:store/rating store) (:store/review-count store))))))

(defui Index
  static om/IQuery
  (query [_]
    [{:proxy/navbar (om/get-query nav/Navbar)}
     {:query/featured-items [:item/name
                             :item/price
                             :item/id
                             :item/img-src
                             :item/store]}
     {:query/featured-stores [:db/id :store/name :store/featured-img-src :store/rating :store/review-count]}
     {:query/featured-streams [:stream/name {:stream/store [:store/name]} :stream/viewer-count :stream/img-src]}])
  Object
  (render [this]
    (let [{:keys [proxy/navbar query/featured-items query/featured-stores query/featured-streams]} (om/props this)]
      (common/page-container
        {:navbar navbar}
        (dom/div #js {:id "sulo-index-container"}

          (dom/div #js {:className "intro-header"}
            (dom/div #js {:className "row"}
              (dom/div #js {:className "column small-offset-6 header-photo-container"}
                (dom/div #js {:className "header-photo"})))
            (dom/div #js {:className "row column header-content"}
              (dom/h1 nil "Go to the market in the comfort of your own home")
              (dom/div #js {:className "search-container row"}
                (dom/div #js {:className "column small-12 medium-6"}
                  (dom/input #js {:placeholder "What are you shopping for?"
                              :type        "text"}))
                (dom/div #js {:className "column"}
                  (dom/a #js {:className "button"}
                         "Search")))))

          (dom/div #js {:className "top-features"}
            (dom/div #js {:className " row small-up-1 medium-up-3"}
              (top-feature
                nil
                "fa-shopping-cart"
                "Connect with the community"
                "Real time interaction with the community.")
              (top-feature
                nil
                "fa-shopping-cart"
                "Find products and stories you love"
                "Our partners have a wide range of home made goods")
              (top-feature
                nil
                "fa-shopping-cart"
                "Share your favorites"
                "Interact with your customers as a store owner and create real long time relationships")))


          ;(banner {:color :red}
          ;        (dom/h2 nil "Interact live with store owners and other buyers from the community"))

          (content-section nil
                           "Stores streaming on the online market right now"
                           (map (fn [c]
                                  (online-channel-element c))
                                featured-streams)
                           "Check out more on the live market >>")

          ;(banner {:color :blue}
          ;        (dom/h2 nil "Find products you love made by passionate store owners"))

          (content-section {:href "/goods"}
                           "Fresh from the oven goods"
                           (map (fn [p]
                                  (common/product-element p {:open-url? true}))
                                featured-items)
                           "Check out more goods >>")

          ;(banner {:color :green}
          ;        (dom/h2 nil "Discover new stores and become part of their community"))

          (content-section nil
                           "Have you seen these stores?"
                           (map (fn [s]
                                  (store-element s))
                                featured-stores)
                           "Check out more stores >>")

          (banner {:color :default}
                  (dom/h2 nil "Follow your favorite stores and stay updated on when they go online")
                  (dom/a #js {:className "button"} "Sign up"))

          (banner {:color :white
                   :align :right}
                  (dom/h2 nil "Start streaming on Sulo and tell your story to your customers")
                  (dom/a #js {:className "button secondary"} "Contact us")))))))

(def ->Index (om/factory Index))