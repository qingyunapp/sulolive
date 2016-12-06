(ns eponai.common.ui.common
  (:require
    [om.dom :as dom]))

(defn rating-element [rating review-count]
  (let [stars (cond-> (vec (repeat rating "fa fa-star fa-fw"))

                      (< 0 (- rating (int rating)))
                      (conj "fa fa-star-half-o fa-fw"))

        empty-stars (repeat (- 5 (count stars)) "fa fa-star-o fa-fw")
        all-stars (concat stars empty-stars)]
    (dom/div {:className "user-rating-container"}
      (apply dom/span nil
             (map (fn [cl]
                    (dom/i {:className cl}))
                  all-stars))
      (dom/span nil (str "(" review-count ")")))))

(defn product-element [product]
  (dom/div {:className "column content-item product-item"}
    (dom/a {:className "content-item-thumbnail-container"
            :href      (str "/goods/" (:item/id product))}
           (dom/div {:className "content-item-thumbnail" :style {:background-image (str "url(" (:item/img-src product) ")")}}))
    (dom/div {:className "content-item-title-section"}
      (dom/a nil (:item/name product)))
    (dom/div {:className "content-item-subtitle-section"}
      (dom/strong nil (:item/price product))
      (rating-element 5 11))))