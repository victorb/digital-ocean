(ns digitalocean.v2.core
  (:require [cheshire.core :as json]
	    [schema.core :as scm]
            [clojure.java.io :as io]
	    [org.httpkit.client :as http]))

(def endpoint "https://api.digitalocean.com/v2/")

(defn load-dev-token
  "Loads temporary token for development
   can be removed at some point"
  []
  (let [path "/Users/owainlewis/.auth/DIGITALOCEAN.txt"]
    (with-open [rdr (io/reader path)]
      (first (take 1 (line-seq rdr))))))

(defn run-request
  "Utility method for making HTTP requests
   to the Digital Ocean API"
  [method url token & params]
  (let [all-params (into {} params)
        {:keys [status headers body error] :as resp}
          @(http/request
            {:method method
             :url url
             :form-params all-params
             :headers {"Authorization" (str "Bearer " token)}})]
  (if (nil? error)
    (json/parse-string body true)
    {:error error})))

(defn resource-url
  "Helper function that builds url endpoints
   (resource-url :domains 1 2 3) =>
     https://api.digitalocean.com/v2/domains/1/2/3
  "
  [resource & parts]
  (let [nested-url-parts (apply str (interpose "/" (into [] parts)))
        qualified-resource (name resource)]
    (str endpoint qualified-resource "/" nested-url-parts)))

;; Generics

(defn generic
  [method resource]
  (let [f (fn [token url-identifiers & params]
            (let [resource-endpoint
                    (apply (partial resource-url
                      (name resource)) url-identifiers)]
              (run-request method resource-endpoint token (into {} params))))]
  (fn
    ([token]
      (f token [] {}))
    ([token resource-identifier & params]
      (f token [resource-identifier] (into {} params))))))


;; Domains

(def domains (generic :get :domains))

(def get-domain  domains)

;; Droplets

(def droplets (generic :get :droplets))

(def get-droplet droplets)

(def create-droplet (generic :post :droplets))

;; Images

(def images (generic :get :images))

(def get-image images)

;; Keys

(def keys (generic :get :keys))

(def get-key keys)

;; Regions

(def regions (generic :get :regions))

(def get-region regions)

;; Sizes

(def sizes (generic :get :sizes))
