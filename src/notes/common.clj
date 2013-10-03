(ns noir-bootstrap.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js html5]]))

(defn navLink [name link curentPage]
  [:ul.nav (if (= name curentPage) [:li.active [:a {"href" link} name]]
    [:li [:a {"href" link} name]])])

   (defpartial layout [page content]
     (html5
       [:head [:title "noir-bootstrap"]
        (include-css "/css/bootstrap.css")
        (include-css "/css/bootstrap-responsive.css")
        [:style "body { padding-top: 60px; }"]]
       [:body (list
         [:div.navbar.navbar-fixed-top {"data-toggle" "collapse" "data-target" ".nav-collapse"}
          [:div.navbar-inner [:div.container [:a.btn.btn-navbar [:span.icon-bar ]]
                              [:a.brand "Quick Wiki"]
                              [:div.nav-collapse (navLink "Home" "/Home" page) (navLink "All" "/all" page)]
                              ]
           ]
          ]
         [:div.container content]
         (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
         (include-js "/js/bootstrap.min.js"))]))
