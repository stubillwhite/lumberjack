(ns client.components
  (:require [antizer.reagent :as ant]))

(defn page-description
  "A title and short description for a page."
  [title description]
  [:div
   [:h2 title]
   [:p description]])

(def form-style {:label-col   {:span 3}
                 :wrapper-col {:span 6}})

;; Form components
;; --------------------------------------------------

(defn form-button
  "Return a button for the form, with the specified ID and click
  handler."
  [form label on-click]
  (let [submit #(do (.preventDefault %) (on-click form))]
    [ant/form-item {}
     [ant/button {:type "primary" :on-click submit} label]]))

;; TODO Remove this? Or form-checkbox-grouped?
(defn form-checkbox-group
  "Return a checkbox group for the form, where ID is the identifier for
  the group, and labels is a seq of labels for the checkboxes.
  Optionally provide a function to call when the value is changed."
  [form id labels & {:keys [initial-value on-change]
                     :or   {initial-value []
                            on-change     (fn [x])}}]
  (let [options (map (fn [x] {:label x :value x}) labels)]
    [ant/form-item {}
     [ant/decorate-field form id {:initial-value initial-value}
      [ant/checkbox-group {:options  options
                           :style {:width "100%"}
                           :onChange (fn [x] (on-change (js->clj x)))}]]]))

(defn form-checkbox-grouped
  "Return a checkbox group for the form, where ID is the identifier for
  the group, and labels is a seq of labels for the checkboxes.
  Optionally provide a function to call when the value is changed."
  [form id labels & {:keys [initial-value on-change]
                     :or   {initial-value []
                            on-change     (fn [x])}}]
  (let [options (map (fn [x] {:label x :value x}) labels)]
    [ant/form-item {}
     [ant/decorate-field form id {:initial-value initial-value}
      [ant/checkbox-group {:style {:width "100%"}
                           :onChange (fn [x] (on-change (js->clj x)))}
       [ant/row
        (for [x labels]
          [ant/col {:span "12" :key x} [ant/checkbox {:value x} x]])]]]]))

(defn form-input
  "Return an input field for the form with the specified ID and label.
  Optionally provide an initial value, a flag to make the field
  mandatory, a type for validation, and a function to call when the
  value is changed."
  [form id label & {:keys [initial-value required type on-change]
                    :or   {initial-value ""
                           required      false
                           type          "string"
                           on-change     (fn [x])}}]
  [ant/form-item {:label label}
   (ant/decorate-field form id {:initial-value initial-value :rules [{:required required} {:type type}]
                                :onChange (fn [x] (on-change (-> x .-target .-value)))}
                       [ant/input {:placeholder label}])])

(defn form-date
  "Return an date field for the form with the specified ID and label.
  Optionally provide an initial value, a flag to make the field
  mandatory, and a function to call when the value is changed."
  [form id label & {:keys [initial-value required on-change]
                    :or   {initial-value ""
                           required      false
                           on-change     (fn [x])}}]
  [ant/form-item {:label label}
   (ant/decorate-field form id {:initial-value initial-value :rules [{:required required} {:type "object"}]
                                ;; :onChange (fn [x] (on-change (-> x .-target .-value)))
                                }
                       [ant/date-picker {}])])
