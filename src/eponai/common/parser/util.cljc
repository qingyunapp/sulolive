(ns eponai.common.parser.util
  (:refer-clojure :exclude [proxy])
  (:require [taoensso.timbre #?(:clj :refer :cljs :refer-macros) [debug]]
            [om.next :as om]))

(defn post-process-parse
  "Calls post-parse-fn for each [k v] in the result of a (parser env query-expr).
  The post-parse-fn should be a 3 arity function taking [env key value].
  The return of the post-parse-fn will be assoced to the key k. The key can also be removed
  from the parsed result by returning :dissoc from the post-parse-fn."
  ([post-parse-fn] (post-process-parse post-parse-fn []))
  ([post-parse-fn keys-to-process-first]
   (fn
     ([env parsed-result]
      (letfn [(post-parse-subset [result subset]
                (reduce-kv (fn [m k v]
                             (let [ret (post-parse-fn env k v)]
                               (cond
                                 (or (nil? ret)  (identical? ret v)) m
                                 (= ret :dissoc) (dissoc m k)
                                 ;; For proxies: Merge the returned value into the resulting map
                                 ;;              Using merge-with merge incase keys are matching
                                 ;;              in the end result.
                                 (= ret :merge)  (merge-with merge
                                                             (assoc m k {})
                                                             (post-parse-subset v v))
                                 ;; For proxies: Call post-parse-fn for the values returned.
                                 (= ret :call)   (assoc m k (post-parse-subset v v))
                                 :else (assoc m k ret))))
                           result
                           subset))]
        (reduce post-parse-subset
                parsed-result
                [(select-keys parsed-result keys-to-process-first)
                 (apply dissoc parsed-result keys-to-process-first)]))))))

(defn unwrap-proxies
  "Takes a query that's been wrapped with proxies. Unwraps them here.

  This HOPEFULLY doesn't screw us over in the future."
  [query]
  (reduce (fn [q x] (if (vector? x)
                      (into q x)
                      (conj q x)))
          []
          query))

(defn cache-last-read
  "Takes a read function caches its last call if data
  arguments are equal. (e.g. params, :db, :target and :query)."
  [f]
  (let [last-call (atom nil)]
    (fn [& args]
      (let [[env k params] args
            [[last-env _ last-params] last-ret] @last-call
            equal-key? (fn [k] (= (get env k)
                                  (get last-env k)))]
        (if (and (= params last-params)
                 (every? equal-key? [:target :query :db]))
          (do
            (prn (str "Returning cached for:" k))
            last-ret)
          (let [ret (apply f args)]
            (reset! last-call [args ret])
            ret))))))

(defn put-db-id-in-query [query]
  (cond (map? query)
        (reduce-kv (fn [q k v]
                     (assoc q k (put-db-id-in-query v)))
                   {}
                   query)

        (sequential? query)
        (->> query
             (remove #(= :db/id %))
             (map put-db-id-in-query)
             (cons :db/id)
             (into []))

        :else
        query))

(defn proxy [{:keys [parser query target] :as env} k _]
  (let [ret (parser env query target)]
    #?(:clj  {:value ret}
       :cljs (if (and target (seq ret))
               ;; Trial and error led to this solution.
               ;; For some reason "proxy" keys get nested in one too many vectors
               ;; and we need to unwrap them here.
               {target (om/query->ast [{k (unwrap-proxies ret)}])}
               {:value ret}))))

(defn return
  "Special read key (special like :proxy) that just returns
  whatever it is bound to.

  Example:
  om/IQuery
  (query [this] ['(:return/foo {:value 1 :remote true})])
  Will always return 1 and be remote true."
  [_ _ p]
  p)