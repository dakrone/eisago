{:project {:_all {:enabled true}
           :_source {:enabled false}
           :properties {:id {:store "yes"
                             :index "not_analyzed"
                             :type "string"}
                        :name {:store "yes" :type "string"}
                        :group {:store "yes" :type "string"}
                        :version {:store "yes" :type "string"}
                        :url {:store "yes" :type "string"}
                        :description {:store "yes"
                                      :term_vector "with_positions_offsets"
                                      :type "string"}
                        :licence {:store "yes" :type "string"}
                        :namespaces {:store "yes" :type "string"}
                        ;; date indexed
                        :index-date {:store "yes"
                                     :type "date"
                                     :format "yyyy-MM-dd'T'HH:mm:ss'Z'"}}}
 :var {:_all {:enabled true}
       :_source {:enabled false}
       :properties {:id {:store "yes"
                         :index "not_analyzed"
                         :type "string"}
                    :project {:store "yes" :type "string" :index "not_analyzed"}
                    :name {:store "yes" :type "string" :index "not_analyzed"}
                    :ns {:store "yes" :type "string" :index "not_analyzed"}
                    :arglists {:store "yes"
                               :type "string"
                               :index "not_analyzed"}
                    :library {:store "yes" :type "string" :index "not_analyzed"}
                    :lib-version {:store "yes" :type "string"}
                    :line {:store "yes" :type "integer"}
                    :file {:store "yes" :type "string"}
                    :doc {:store "yes"
                          :term_vector "with_positions_offsets"
                          :type "string"}
                    :source {:store "yes"
                             :index "not_analyzed"
                             :type "string"}
                    ;; date indexed
                    :index-date {:store "yes"
                                 :type "date"
                                 :format "yyyy-MM-dd'T'HH:mm:ss'Z'"}}}

 :example {:_all {:enabled true}
           :_source {:enabled false}
           :_parent {:type "var"}
           :properties {:id {:store "yes"
                             :index "not_analyzed"
                             :type "string"}
                        :body {:store "yes"
                               :term_vector "with_positions_offsets"
                               :type "string"}
                        :parent-id {:store "yes"
                                    :index "not_analyzed"
                                    :type "string"}
                        ;; date indexed
                        :index-date {:store "yes"
                                     :type "date"
                                     :format "yyyy-MM-dd'T'HH:mm:ss'Z'"}}}
 :comment {:_all {:enabled true}
           :_source {:enabled false}
           :_parent {:type "var"}
           :properties {:id {:store "yes"
                             :index "not_analyzed"
                             :type "string"}
                        :body {:store "yes"
                               :term_vector "with_positions_offsets"
                               :type "string"}
                        :parent-id {:store "yes"
                                    :index "not_analyzed"
                                    :type "string"}
                        ;; date indexed
                        :index-date {:store "yes"
                                     :type "date"
                                     :format "yyyy-MM-dd'T'HH:mm:ss'Z'"}}}}
