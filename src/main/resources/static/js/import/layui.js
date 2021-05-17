/** layui v2.6.5 | Released under the MIT license */
;!function (t) {
    "use strict";
    var e = document, n = {modules: {}, status: {}, timeout: 10, event: {}}, r = function () {
        this.v = "2.6.5"
    }, o = function () {
        var t = e.currentScript ? e.currentScript.src : function () {
            for (var t, n = e.scripts, r = n.length - 1, o = r; o > 0; o--) if ("interactive" === n[o].readyState) {
                t = n[o].src;
                break
            }
            return t || n[r].src
        }();
        return n.dir = t.substring(0, t.lastIndexOf("/") + 1)
    }(), a = function (e, n) {
        n = n || "log", t.console && console[n] && console[n]("layui error hint: " + e)
    }, i = "undefined" != typeof opera && "[object Opera]" === opera.toString(), u = n.builtin = {
        lay: "lay",
        layer: "layer",
        laydate: "laydate",
        laypage: "laypage",
        laytpl: "laytpl",
        layedit: "layedit",
        form: "form",
        upload: "upload",
        dropdown: "dropdown",
        transfer: "transfer",
        tree: "tree",
        table: "table",
        element: "element",
        rate: "rate",
        colorpicker: "colorpicker",
        slider: "slider",
        carousel: "carousel",
        flow: "flow",
        util: "util",
        code: "code",
        jquery: "jquery",
        all: "all",
        "layui.all": "layui.all"
    };
    r.prototype.cache = n, r.prototype.define = function (t, e) {
        var r = this, o = "function" == typeof t, a = function () {
            var t = function (t, e) {
                layui[t] = e, n.status[t] = !0
            };
            return "function" == typeof e && e(function (r, o) {
                t(r, o), n.callback[r] = function () {
                    e(t)
                }
            }), this
        };
        return o && (e = t, t = []), r.use(t, a, null, "define"), r
    }, r.prototype.use = function (t, r, l, c) {
        function s(t, e) {
            var r = "PLaySTATION 3" === navigator.platform ? /^complete$/ : /^(complete|loaded)$/;
            ("load" === t.type || r.test((t.currentTarget || t.srcElement).readyState)) && (n.modules[v] = e, d.removeChild(g), function o() {
                return ++h > 1e3 * n.timeout / 4 ? a(v + " is not a valid module", "error") : void (n.status[v] ? p() : setTimeout(o, 4))
            }())
        }

        function p() {
            l.push(layui[v]), t.length > 1 ? f.use(t.slice(1), r, l, c) : "function" == typeof r && function () {
                return layui.jquery && "function" == typeof layui.jquery && "define" !== c ? layui.jquery(function () {
                    r.apply(layui, l)
                }) : void r.apply(layui, l)
            }()
        }

        var f = this, y = n.dir = n.dir ? n.dir : o, d = e.getElementsByTagName("head")[0];
        t = function () {
            return "string" == typeof t ? [t] : "function" == typeof t ? (r = t, ["all"]) : t
        }(), window.jQuery && jQuery.fn.on && (f.each(t, function (e, n) {
            "jquery" === n && t.splice(e, 1)
        }), layui.jquery = layui.$ = jQuery);
        var v = t[0], h = 0;
        if (l = l || [], n.host = n.host || (y.match(/\/\/([\s\S]+?)\//) || ["//" + location.host + "/"])[0], 0 === t.length || layui["layui.all"] && u[v]) return p(), f;
        var m = (u[v] ? y + "modules/" : /^\{\/\}/.test(f.modules[v]) ? "" : n.base || "") + (f.modules[v] || v) + ".js";
        if (m = m.replace(/^\{\/\}/, ""), !n.modules[v] && layui[v] && (n.modules[v] = m), n.modules[v]) !function b() {
            return ++h > 1e3 * n.timeout / 4 ? a(v + " is not a valid module", "error") : void ("string" == typeof n.modules[v] && n.status[v] ? p() : setTimeout(b, 4))
        }(); else {
            var g = e.createElement("script");
            g.async = !0, g.charset = "utf-8", g.src = m + function () {
                var t = n.version === !0 ? n.v || (new Date).getTime() : n.version || "";
                return t ? "?v=" + t : ""
            }(), d.appendChild(g), !g.attachEvent || g.attachEvent.toString && g.attachEvent.toString().indexOf("[native code") < 0 || i ? g.addEventListener("load", function (t) {
                s(t, m)
            }, !1) : g.attachEvent("onreadystatechange", function (t) {
                s(t, m)
            }), n.modules[v] = m
        }
        return f
    }, r.prototype.getStyle = function (e, n) {
        var r = e.currentStyle ? e.currentStyle : t.getComputedStyle(e, null);
        return r[r.getPropertyValue ? "getPropertyValue" : "getAttribute"](n)
    }, r.prototype.link = function (t, r, o) {
        var i = this, u = e.getElementsByTagName("head")[0], l = e.createElement("link");
        "string" == typeof r && (o = r);
        var c = (o || t).replace(/\.|\//g, ""), s = l.id = "layuicss-" + c, p = "creating", f = 0;
        return l.rel = "stylesheet", l.href = t + (n.debug ? "?v=" + (new Date).getTime() : ""), l.media = "all", e.getElementById(s) || u.appendChild(l), "function" != typeof r ? i : (function y(o) {
            var u = 100, l = e.getElementById(s);
            return ++f > 1e3 * n.timeout / u ? a(t + " timeout") : void (1989 === parseInt(i.getStyle(l, "width")) ? (o === p && l.removeAttribute("lay-status"), l.getAttribute("lay-status") === p ? setTimeout(y, u) : r()) : (l.setAttribute("lay-status", p), setTimeout(function () {
                y(p)
            }, u)))
        }(), i)
    }, r.prototype.addcss = function (t, e, r) {
        return layui.link(n.dir + "css/" + t, e, r)
    }, n.callback = {}, r.prototype.factory = function (t) {
        if (layui[t]) return "function" == typeof n.callback[t] ? n.callback[t] : null
    }, r.prototype.img = function (t, e, n) {
        var r = new Image;
        return r.src = t, r.complete ? e(r) : (r.onload = function () {
            r.onload = null, "function" == typeof e && e(r)
        }, void (r.onerror = function (t) {
            r.onerror = null, "function" == typeof n && n(t)
        }))
    }, r.prototype.config = function (t) {
        t = t || {};
        for (var e in t) n[e] = t[e];
        return this
    }, r.prototype.modules = function () {
        var t = {};
        for (var e in u) t[e] = u[e];
        return t
    }(), r.prototype.extend = function (t) {
        var e = this;
        t = t || {};
        for (var n in t) e[n] || e.modules[n] ? a(n + " Module already exists", "error") : e.modules[n] = t[n];
        return e
    }, r.prototype.router = function (t) {
        var e = this, t = t || location.hash, n = {path: [], search: {}, hash: (t.match(/[^#](#.*$)/) || [])[1] || ""};
        return /^#\//.test(t) ? (t = t.replace(/^#\//, ""), n.href = "/" + t, t = t.replace(/([^#])(#.*$)/, "$1").split("/") || [], e.each(t, function (t, e) {
            /^\w+=/.test(e) ? function () {
                e = e.split("="), n.search[e[0]] = e[1]
            }() : n.path.push(e)
        }), n) : n
    }, r.prototype.url = function (t) {
        var e = this, n = {
            pathname: function () {
                var e = t ? function () {
                    var e = (t.match(/\.[^.]+?\/.+/) || [])[0] || "";
                    return e.replace(/^[^\/]+/, "").replace(/\?.+/, "")
                }() : location.pathname;
                return e.replace(/^\//, "").split("/")
            }(), search: function () {
                var n = {}, r = (t ? function () {
                    var e = (t.match(/\?.+/) || [])[0] || "";
                    return e.replace(/\#.+/, "")
                }() : location.search).replace(/^\?+/, "").split("&");
                return e.each(r, function (t, e) {
                    var r = e.indexOf("="), o = function () {
                        return r < 0 ? e.substr(0, e.length) : 0 !== r && e.substr(0, r)
                    }();
                    o && (n[o] = r > 0 ? e.substr(r + 1) : null)
                }), n
            }(), hash: e.router(function () {
                return t ? (t.match(/#.+/) || [])[0] || "" : location.hash
            }())
        };
        return n
    }, r.prototype.data = function (e, n, r) {
        if (e = e || "layui", r = r || localStorage, t.JSON && t.JSON.parse) {
            if (null === n) return delete r[e];
            n = "object" == typeof n ? n : {key: n};
            try {
                var o = JSON.parse(r[e])
            } catch (a) {
                var o = {}
            }
            return "value" in n && (o[n.key] = n.value), n.remove && delete o[n.key], r[e] = JSON.stringify(o), n.key ? o[n.key] : o
        }
    }, r.prototype.sessionData = function (t, e) {
        return this.data(t, e, sessionStorage)
    }, r.prototype.device = function (e) {
        var n = navigator.userAgent.toLowerCase(), r = function (t) {
            var e = new RegExp(t + "/([^\\s\\_\\-]+)");
            return t = (n.match(e) || [])[1], t || !1
        }, o = {
            os: function () {
                return /windows/.test(n) ? "windows" : /linux/.test(n) ? "linux" : /iphone|ipod|ipad|ios/.test(n) ? "ios" : /mac/.test(n) ? "mac" : void 0
            }(), ie: function () {
                return !!(t.ActiveXObject || "ActiveXObject" in t) && ((n.match(/msie\s(\d+)/) || [])[1] || "11")
            }(), weixin: r("micromessenger")
        };
        return e && !o[e] && (o[e] = r(e)), o.android = /android/.test(n), o.ios = "ios" === o.os, o.mobile = !(!o.android && !o.ios), o
    }, r.prototype.hint = function () {
        return {error: a}
    }, r.prototype.each = function (t, e) {
        var n, r = this;
        if ("function" != typeof e) return r;
        if (t = t || [], t.constructor === Object) {
            for (n in t) if (e.call(t[n], n, t[n])) break
        } else for (n = 0; n < t.length && !e.call(t[n], n, t[n]); n++) ;
        return r
    }, r.prototype.sort = function (t, e, n) {
        var r = JSON.parse(JSON.stringify(t || []));
        return e ? (r.sort(function (t, n) {
            var r = /^-?\d+$/, o = t[e], a = n[e];
            return r.test(o) && (o = parseFloat(o)), r.test(a) && (a = parseFloat(a)), o && !a ? 1 : !o && a ? -1 : o > a ? 1 : o < a ? -1 : 0
        }), n && r.reverse(), r) : r
    }, r.prototype.stope = function (e) {
        e = e || t.event;
        try {
            e.stopPropagation()
        } catch (n) {
            e.cancelBubble = !0
        }
    }, r.prototype.onevent = function (t, e, n) {
        return "string" != typeof t || "function" != typeof n ? this : r.event(t, e, null, n)
    }, r.prototype.event = r.event = function (t, e, r, o) {
        var a = this, i = null, u = (e || "").match(/\((.*)\)$/) || [], l = (t + "." + e).replace(u[0], ""),
            c = u[1] || "", s = function (t, e) {
                var n = e && e.call(a, r);
                n === !1 && null === i && (i = !1)
            };
        return "LAYUI-EVENT-REMOVE" === r ? (delete (a.cache.event[l] || {})[c], a) : o ? (n.event[l] = n.event[l] || {}, n.event[l][c] = [o], this) : (layui.each(n.event[l], function (t, e) {
            return "{*}" === c ? void layui.each(e, s) : ("" === t && layui.each(e, s), void (c && t === c && layui.each(e, s)))
        }), i)
    }, r.prototype.on = function (t, e, n) {
        var r = this;
        return r.onevent.call(r, e, t, n)
    }, r.prototype.off = function (t, e) {
        var n = this;
        return n.event.call(n, e, t, "LAYUI-EVENT-REMOVE")
    }, t.layui = new r
}(window);
layui.define(function (a) {
    var i = layui.cache;
    layui.config({dir: i.dir.replace(/lay\/dest\/$/, "")}), a("layui.all", layui.v)
});
!function () {
    "use strict";
    var t = "lay", e = window.document, n = function (t) {
        return new r(t)
    }, r = function (t) {
        for (var n = 0, r = "object" == typeof t ? [t] : (this.selector = t, e.querySelectorAll(t || null)); n < r.length; n++) this.push(r[n])
    };
    r.prototype = [], r.prototype.constructor = r, n.extend = function () {
        var t = 1, e = arguments, n = function (t, e) {
            t = t || (e.constructor === Array ? [] : {});
            for (var r in e) t[r] = e[r] && e[r].constructor === Object ? n(t[r], e[r]) : e[r];
            return t
        };
        for (e[0] = "object" == typeof e[0] ? e[0] : {}; t < e.length; t++) "object" == typeof e[t] && n(e[0], e[t]);
        return e[0]
    }, n.v = "1.0.0", n.ie = function () {
        var t = navigator.userAgent.toLowerCase();
        return !!(window.ActiveXObject || "ActiveXObject" in window) && ((t.match(/msie\s(\d+)/) || [])[1] || "11")
    }(), n.getPath = function () {
        var t = e.currentScript ? e.currentScript.src : function () {
            for (var t, n = e.scripts, r = n.length - 1, o = r; o > 0; o--) if ("interactive" === n[o].readyState) {
                t = n[o].src;
                break
            }
            return t || n[r].src
        }();
        return t.substring(0, t.lastIndexOf("/") + 1)
    }, n.stope = function (t) {
        t = t || window.event, t.stopPropagation ? t.stopPropagation() : t.cancelBubble = !0
    }, n.each = function (t, e) {
        var n, r = this;
        if ("function" != typeof e) return r;
        if (t = t || [], t.constructor === Object) {
            for (n in t) if (e.call(t[n], n, t[n])) break
        } else for (n = 0; n < t.length && !e.call(t[n], n, t[n]); n++) ;
        return r
    }, n.digit = function (t, e, n) {
        var r = "";
        t = String(t), e = e || 2;
        for (var o = t.length; o < e; o++) r += "0";
        return t < Math.pow(10, e) ? r + (0 | t) : t
    }, n.elem = function (t, r) {
        var o = e.createElement(t);
        return n.each(r || {}, function (t, e) {
            o.setAttribute(t, e)
        }), o
    }, n.getStyle = function (t, e) {
        var n = t.currentStyle ? t.currentStyle : window.getComputedStyle(t, null);
        return n[n.getPropertyValue ? "getPropertyValue" : "getAttribute"](e)
    }, n.link = function (t, r, o) {
        var i = e.getElementsByTagName("head")[0], c = e.createElement("link");
        "string" == typeof r && (o = r);
        var u = (o || t).replace(/\.|\//g, ""), a = "layuicss-" + u, s = "creating", l = 0;
        c.rel = "stylesheet", c.href = t, c.id = a, e.getElementById(a) || i.appendChild(c), "function" == typeof r && !function f(t) {
            var o = 100, i = e.getElementById(a);
            return ++l > 1e4 / o ? window.console && console.error(u + ".css: Invalid") : void (1989 === parseInt(n.getStyle(i, "width")) ? (t === s && i.removeAttribute("lay-status"), i.getAttribute("lay-status") === s ? setTimeout(f, o) : r()) : (i.setAttribute("lay-status", s), setTimeout(function () {
                f(s)
            }, o)))
        }()
    }, n.hasScrollbar = function () {
        return e.body.scrollHeight > (window.innerHeight || e.documentElement.clientHeight)
    }, n.position = function (t, r, o) {
        if (r) {
            o = o || {}, t !== e && t !== n("body")[0] || (o.clickType = "right");
            var i = "right" === o.clickType ? function () {
                var t = o.e || window.event || {};
                return {left: t.clientX, top: t.clientY, right: t.clientX, bottom: t.clientY}
            }() : t.getBoundingClientRect(), c = r.offsetWidth, u = r.offsetHeight, a = function (t) {
                return t = t ? "scrollLeft" : "scrollTop", e.body[t] | e.documentElement[t]
            }, s = function (t) {
                return e.documentElement[t ? "clientWidth" : "clientHeight"]
            }, l = 5, f = i.left, h = i.bottom;
            f + c + l > s("width") && (f = s("width") - c - l), h + u + l > s() && (i.top > u + l ? h = i.top - u - 2 * l : "right" === o.clickType && (h = s() - u - 2 * l, h < 0 && (h = 0)));
            var p = o.position;
            if (p && (r.style.position = p), r.style.left = f + ("fixed" === p ? 0 : a(1)) + "px", r.style.top = h + ("fixed" === p ? 0 : a()) + "px", !n.hasScrollbar()) {
                var d = r.getBoundingClientRect();
                !o.SYSTEM_RELOAD && d.bottom + l > s() && (o.SYSTEM_RELOAD = !0, setTimeout(function () {
                    n.position(t, r, o)
                }, 50))
            }
        }
    }, n.options = function (t, e) {
        var r = n(t), o = e || "lay-options";
        try {
            return new Function("return " + (r.attr(o) || "{}"))()
        } catch (i) {
            return hint.error("parseerror：" + i, "error"), {}
        }
    }, n.isTopElem = function (t) {
        var r = [e, n("body")[0]], o = !1;
        return n.each(r, function (e, n) {
            if (n === t) return o = !0
        }), o
    }, r.addStr = function (t, e) {
        return t = t.replace(/\s+/, " "), e = e.replace(/\s+/, " ").split(" "), n.each(e, function (e, n) {
            new RegExp("\\b" + n + "\\b").test(t) || (t = t + " " + n)
        }), t.replace(/^\s|\s$/, "")
    }, r.removeStr = function (t, e) {
        return t = t.replace(/\s+/, " "), e = e.replace(/\s+/, " ").split(" "), n.each(e, function (e, n) {
            var r = new RegExp("\\b" + n + "\\b");
            r.test(t) && (t = t.replace(r, ""))
        }), t.replace(/\s+/, " ").replace(/^\s|\s$/, "")
    }, r.prototype.find = function (t) {
        var e = this, r = 0, o = [], i = "object" == typeof t;
        return this.each(function (n, c) {
            for (var u = i ? [t] : c.querySelectorAll(t || null); r < u.length; r++) o.push(u[r]);
            e.shift()
        }), i || (e.selector = (e.selector ? e.selector + " " : "") + t), n.each(o, function (t, n) {
            e.push(n)
        }), e
    }, r.prototype.each = function (t) {
        return n.each.call(this, this, t)
    }, r.prototype.addClass = function (t, e) {
        return this.each(function (n, o) {
            o.className = r[e ? "removeStr" : "addStr"](o.className, t)
        })
    }, r.prototype.removeClass = function (t) {
        return this.addClass(t, !0)
    }, r.prototype.hasClass = function (t) {
        var e = !1;
        return this.each(function (n, r) {
            new RegExp("\\b" + t + "\\b").test(r.className) && (e = !0)
        }), e
    }, r.prototype.css = function (t, e) {
        var r = this, o = function (t) {
            return isNaN(t) ? t : t + "px"
        };
        return "string" == typeof t && void 0 === e ? function () {
            if (r.length > 0) return r[0].style[t]
        }() : r.each(function (r, i) {
            "object" == typeof t ? n.each(t, function (t, e) {
                i.style[t] = o(e)
            }) : i.style[t] = o(e)
        })
    }, r.prototype.width = function (t) {
        var e = this;
        return void 0 === t ? function () {
            if (e.length > 0) return e[0].offsetWidth
        }() : e.each(function (n, r) {
            e.css("width", t)
        })
    }, r.prototype.height = function (t) {
        var e = this;
        return void 0 === t ? function () {
            if (e.length > 0) return e[0].offsetHeight
        }() : e.each(function (n, r) {
            e.css("height", t)
        })
    }, r.prototype.attr = function (t, e) {
        var n = this;
        return void 0 === e ? function () {
            if (n.length > 0) return n[0].getAttribute(t)
        }() : n.each(function (n, r) {
            r.setAttribute(t, e)
        })
    }, r.prototype.removeAttr = function (t) {
        return this.each(function (e, n) {
            n.removeAttribute(t)
        })
    }, r.prototype.html = function (t) {
        var e = this;
        return void 0 === t ? function () {
            if (e.length > 0) return e[0].innerHTML
        }() : this.each(function (e, n) {
            n.innerHTML = t
        })
    }, r.prototype.val = function (t) {
        return void 0 === t ? function () {
            if (that.length > 0) return that[0].value
        }() : this.each(function (e, n) {
            n.value = t
        })
    }, r.prototype.append = function (t) {
        return this.each(function (e, n) {
            "object" == typeof t ? n.appendChild(t) : n.innerHTML = n.innerHTML + t
        })
    }, r.prototype.remove = function (t) {
        return this.each(function (e, n) {
            t ? n.removeChild(t) : n.parentNode.removeChild(n)
        })
    }, r.prototype.on = function (t, e) {
        return this.each(function (n, r) {
            r.attachEvent ? r.attachEvent("on" + t, function (t) {
                t.target = t.srcElement, e.call(r, t)
            }) : r.addEventListener(t, e, !1)
        })
    }, r.prototype.off = function (t, e) {
        return this.each(function (n, r) {
            r.detachEvent ? r.detachEvent("on" + t, e) : r.removeEventListener(t, e, !1)
        })
    }, window.lay = n, window.layui && layui.define && layui.define(function (e) {
        e(t, n)
    })
}();
layui.define(function (e) {
    "use strict";
    var r = {open: "{{", close: "}}"}, c = {
        exp: function (e) {
            return new RegExp(e, "g")
        }, query: function (e, c, t) {
            var o = ["#([\\s\\S])+?", "([^{#}])*?"][e || 0];
            return n((c || "") + r.open + o + r.close + (t || ""))
        }, escape: function (e) {
            return String(e || "").replace(/&(?!#?[a-zA-Z0-9]+;)/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#39;").replace(/"/g, "&quot;")
        }, error: function (e, r) {
            var c = "Laytpl Error: ";
            return "object" == typeof console && console.error(c + e + "\n" + (r || "")), c + e
        }
    }, n = c.exp, t = function (e) {
        this.tpl = e
    };
    t.pt = t.prototype, window.errors = 0, t.pt.parse = function (e, t) {
        var o = this, p = e, a = n("^" + r.open + "#", ""), l = n(r.close + "$", "");
        e = e.replace(/\s+|\r|\t|\n/g, " ").replace(n(r.open + "#"), r.open + "# ").replace(n(r.close + "}"), "} " + r.close).replace(/\\/g, "\\\\").replace(n(r.open + "!(.+?)!" + r.close), function (e) {
            return e = e.replace(n("^" + r.open + "!"), "").replace(n("!" + r.close), "").replace(n(r.open + "|" + r.close), function (e) {
                return e.replace(/(.)/g, "\\$1")
            })
        }).replace(/(?="|')/g, "\\").replace(c.query(), function (e) {
            return e = e.replace(a, "").replace(l, ""), '";' + e.replace(/\\(.)/g, "$1") + ';view+="'
        }).replace(c.query(1), function (e) {
            var c = '"+(';
            return e.replace(/\s/g, "") === r.open + r.close ? "" : (e = e.replace(n(r.open + "|" + r.close), ""), /^=/.test(e) && (e = e.replace(/^=/, ""), c = '"+_escape_('), c + e.replace(/\\(.)/g, "$1") + ')+"')
        }), e = '"use strict";var view = "' + e + '";return view;';
        try {
            return o.cache = e = new Function("d, _escape_", e), e(t, c.escape)
        } catch (u) {
            return delete o.cache, c.error(u, p)
        }
    }, t.pt.render = function (e, r) {
        var n, t = this;
        return e ? (n = t.cache ? t.cache(e, c.escape) : t.parse(t.tpl, e), r ? void r(n) : n) : c.error("no data")
    };
    var o = function (e) {
        return "string" != typeof e ? c.error("Template not found") : new t(e)
    };
    o.config = function (e) {
        e = e || {};
        for (var c in e) r[c] = e[c]
    }, o.v = "1.2.0", e("laytpl", o)
});
layui.define(function (e) {
    "use strict";
    var a = document, t = "getElementById", n = "getElementsByTagName", i = "laypage", r = "layui-disabled",
        u = function (e) {
            var a = this;
            a.config = e || {}, a.config.index = ++s.index, a.render(!0)
        };
    u.prototype.type = function () {
        var e = this.config;
        if ("object" == typeof e.elem) return void 0 === e.elem.length ? 2 : 3
    }, u.prototype.view = function () {
        var e = this, a = e.config, t = a.groups = "groups" in a ? 0 | a.groups : 5;
        a.layout = "object" == typeof a.layout ? a.layout : ["prev", "page", "next"], a.count = 0 | a.count, a.curr = 0 | a.curr || 1, a.limits = "object" == typeof a.limits ? a.limits : [10, 20, 30, 40, 50], a.limit = 0 | a.limit || 10, a.pages = Math.ceil(a.count / a.limit) || 1, a.curr > a.pages && (a.curr = a.pages), t < 0 ? t = 1 : t > a.pages && (t = a.pages), a.prev = "prev" in a ? a.prev : "&#x4E0A;&#x4E00;&#x9875;", a.next = "next" in a ? a.next : "&#x4E0B;&#x4E00;&#x9875;";
        var n = a.pages > t ? Math.ceil((a.curr + (t > 1 ? 1 : 0)) / (t > 0 ? t : 1)) : 1, i = {
            prev: function () {
                return a.prev ? '<a href="javascript:;" class="layui-laypage-prev' + (1 == a.curr ? " " + r : "") + '" data-page="' + (a.curr - 1) + '">' + a.prev + "</a>" : ""
            }(),
            page: function () {
                var e = [];
                if (a.count < 1) return "";
                n > 1 && a.first !== !1 && 0 !== t && e.push('<a href="javascript:;" class="layui-laypage-first" data-page="1"  title="&#x9996;&#x9875;">' + (a.first || 1) + "</a>");
                var i = Math.floor((t - 1) / 2), r = n > 1 ? a.curr - i : 1, u = n > 1 ? function () {
                    var e = a.curr + (t - i - 1);
                    return e > a.pages ? a.pages : e
                }() : t;
                for (u - r < t - 1 && (r = u - t + 1), a.first !== !1 && r > 2 && e.push('<span class="layui-laypage-spr">&#x2026;</span>'); r <= u; r++) r === a.curr ? e.push('<span class="layui-laypage-curr"><em class="layui-laypage-em" ' + (/^#/.test(a.theme) ? 'style="background-color:' + a.theme + ';"' : "") + "></em><em>" + r + "</em></span>") : e.push('<a href="javascript:;" data-page="' + r + '">' + r + "</a>");
                return a.pages > t && a.pages > u && a.last !== !1 && (u + 1 < a.pages && e.push('<span class="layui-laypage-spr">&#x2026;</span>'), 0 !== t && e.push('<a href="javascript:;" class="layui-laypage-last" title="&#x5C3E;&#x9875;"  data-page="' + a.pages + '">' + (a.last || a.pages) + "</a>")), e.join("")
            }(),
            next: function () {
                return a.next ? '<a href="javascript:;" class="layui-laypage-next' + (a.curr == a.pages ? " " + r : "") + '" data-page="' + (a.curr + 1) + '">' + a.next + "</a>" : ""
            }(),
            count: '<span class="layui-laypage-count">共 ' + a.count + " 条</span>",
            limit: function () {
                var e = ['<span class="layui-laypage-limits"><select lay-ignore>'];
                return layui.each(a.limits, function (t, n) {
                    e.push('<option value="' + n + '"' + (n === a.limit ? "selected" : "") + ">" + n + " 条/页</option>")
                }), e.join("") + "</select></span>"
            }(),
            refresh: ['<a href="javascript:;" data-page="' + a.curr + '" class="layui-laypage-refresh">', '<i class="layui-icon layui-icon-refresh"></i>', "</a>"].join(""),
            skip: function () {
                return ['<span class="layui-laypage-skip">&#x5230;&#x7B2C;', '<input type="text" min="1" value="' + a.curr + '" class="layui-input">', '&#x9875;<button type="button" class="layui-laypage-btn">&#x786e;&#x5b9a;</button>', "</span>"].join("")
            }()
        };
        return ['<div class="layui-box layui-laypage layui-laypage-' + (a.theme ? /^#/.test(a.theme) ? "molv" : a.theme : "default") + '" id="layui-laypage-' + a.index + '">', function () {
            var e = [];
            return layui.each(a.layout, function (a, t) {
                i[t] && e.push(i[t])
            }), e.join("")
        }(), "</div>"].join("")
    }, u.prototype.jump = function (e, a) {
        if (e) {
            var t = this, i = t.config, r = e.children, u = e[n]("button")[0], l = e[n]("input")[0],
                p = e[n]("select")[0], c = function () {
                    var e = 0 | l.value.replace(/\s|\D/g, "");
                    e && (i.curr = e, t.render())
                };
            if (a) return c();
            for (var o = 0, y = r.length; o < y; o++) "a" === r[o].nodeName.toLowerCase() && s.on(r[o], "click", function () {
                var e = 0 | this.getAttribute("data-page");
                e < 1 || e > i.pages || (i.curr = e, t.render())
            });
            p && s.on(p, "change", function () {
                var e = this.value;
                i.curr * e > i.count && (i.curr = Math.ceil(i.count / e)), i.limit = e, t.render()
            }), u && s.on(u, "click", function () {
                c()
            })
        }
    }, u.prototype.skip = function (e) {
        if (e) {
            var a = this, t = e[n]("input")[0];
            t && s.on(t, "keyup", function (t) {
                var n = this.value, i = t.keyCode;
                /^(37|38|39|40)$/.test(i) || (/\D/.test(n) && (this.value = n.replace(/\D/, "")), 13 === i && a.jump(e, !0))
            })
        }
    }, u.prototype.render = function (e) {
        var n = this, i = n.config, r = n.type(), u = n.view();
        2 === r ? i.elem && (i.elem.innerHTML = u) : 3 === r ? i.elem.html(u) : a[t](i.elem) && (a[t](i.elem).innerHTML = u), i.jump && i.jump(i, e);
        var s = a[t]("layui-laypage-" + i.index);
        n.jump(s), i.hash && !e && (location.hash = "!" + i.hash + "=" + i.curr), n.skip(s)
    };
    var s = {
        render: function (e) {
            var a = new u(e);
            return a.index
        }, index: layui.laypage ? layui.laypage.index + 1e4 : 0, on: function (e, a, t) {
            return e.attachEvent ? e.attachEvent("on" + a, function (a) {
                a.target = a.srcElement, t.call(e, a)
            }) : e.addEventListener(a, t, !1), this
        }
    };
    e(i, s)
});
!function (e) {
    "use strict";
    var t = e.layui && layui.define, a = {
            getPath: e.lay && lay.getPath ? lay.getPath() : "", link: function (t, a, l) {
                n.path && e.lay && lay.link && lay.link(n.path + t, a, l)
            }
        }, n = {
            v: "5.2.1", config: {}, index: e.laydate && e.laydate.v ? 1e5 : 0, path: a.getPath, set: function (e) {
                var t = this;
                return t.config = lay.extend({}, t.config, e), t
            }, ready: function (e) {
                var l = "laydate", i = "", r = (t ? "modules/laydate/" : "theme/") + "default/laydate.css?v=" + n.v + i;
                // return t ? layui.addcss(r, e, l) : a.link(r, e, l), this
                return null, this
            }
        }, l = function () {
            var e = this;
            return {
                hint: function (t) {
                    e.hint.call(e, t)
                }, config: e.config
            }
        }, i = "laydate", r = ".layui-laydate", o = "layui-this", s = "laydate-disabled", y = [100, 2e5],
        d = "layui-laydate-static", m = "layui-laydate-list", c = "laydate-selected", u = "layui-laydate-hint",
        h = "layui-laydate-footer", f = ".laydate-btns-confirm", p = "laydate-time-text", g = ".laydate-btns-time",
        v = function (e) {
            var t = this;
            t.index = ++n.index, t.config = lay.extend({}, t.config, n.config, e), n.ready(function () {
                t.init()
            })
        };
    v.isLeapYear = function (e) {
        return e % 4 === 0 && e % 100 !== 0 || e % 400 === 0
    }, v.prototype.config = {
        type: "date",
        range: !1,
        format: "yyyy-MM-dd",
        value: null,
        isInitValue: !0,
        min: "1900-1-1",
        max: "2099-12-31",
        trigger: "click",
        show: !1,
        showBottom: !0,
        btns: ["clear", "now", "confirm"],
        lang: "cn",
        theme: "default",
        position: null,
        calendar: !1,
        mark: {},
        zIndex: null,
        done: null,
        change: null
    }, v.prototype.lang = function () {
        var e = this, t = e.config, a = {
            cn: {
                weeks: ["日", "一", "二", "三", "四", "五", "六"],
                time: ["时", "分", "秒"],
                timeTips: "选择时间",
                startTime: "开始时间",
                endTime: "结束时间",
                dateTips: "返回日期",
                month: ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"],
                tools: {confirm: "确定", clear: "清空", now: "现在"},
                timeout: "结束时间不能早于开始时间<br>请重新选择",
                invalidDate: "不在有效日期或时间范围内",
                formatError: ["日期格式不合法<br>必须遵循下述格式：<br>", "<br>已为你重置"]
            },
            en: {
                weeks: ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"],
                time: ["Hours", "Minutes", "Seconds"],
                timeTips: "Select Time",
                startTime: "Start Time",
                endTime: "End Time",
                dateTips: "Select Date",
                month: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
                tools: {confirm: "Confirm", clear: "Clear", now: "Now"},
                timeout: "End time cannot be less than start Time<br>Please re-select",
                invalidDate: "Invalid date",
                formatError: ["The date format error<br>Must be followed：<br>", "<br>It has been reset"]
            }
        };
        return a[t.lang] || a.cn
    }, v.prototype.init = function () {
        var t = this, a = t.config, n = "yyyy|y|MM|M|dd|d|HH|H|mm|m|ss|s", l = "static" === a.position,
            i = {year: "yyyy", month: "yyyy-MM", date: "yyyy-MM-dd", time: "HH:mm:ss", datetime: "yyyy-MM-dd HH:mm:ss"};
        a.elem = lay(a.elem), a.eventElem = lay(a.eventElem), a.elem[0] && (a.range === !0 && (a.range = "-"), i[a.type] || (e.console && console.error && console.error("laydate type error:'" + a.type + "' is not supported"), a.type = "date"), a.format === i.date && (a.format = i[a.type] || i.date), t.format = a.format.match(new RegExp(n + "|.", "g")) || [], t.EXP_IF = "", t.EXP_SPLIT = "", lay.each(t.format, function (e, a) {
            var l = new RegExp(n).test(a) ? "\\d{" + function () {
                return new RegExp(n).test(t.format[0 === e ? e + 1 : e - 1] || "") ? /^yyyy|y$/.test(a) ? 4 : a.length : /^yyyy$/.test(a) ? "1,4" : /^y$/.test(a) ? "1,308" : "1,2"
            }() + "}" : "\\" + a;
            t.EXP_IF = t.EXP_IF + l, t.EXP_SPLIT = t.EXP_SPLIT + "(" + l + ")"
        }), t.EXP_IF = new RegExp("^" + (a.range ? t.EXP_IF + "\\s\\" + a.range + "\\s" + t.EXP_IF : t.EXP_IF) + "$"), t.EXP_SPLIT = new RegExp("^" + t.EXP_SPLIT + "$", ""), t.isInput(a.elem[0]) || "focus" === a.trigger && (a.trigger = "click"), a.elem.attr("lay-key") || (a.elem.attr("lay-key", t.index), a.eventElem.attr("lay-key", t.index)), a.mark = lay.extend({}, a.calendar && "cn" === a.lang ? {
            "0-1-1": "元旦",
            "0-2-14": "情人",
            "0-3-8": "妇女",
            "0-3-12": "植树",
            "0-4-1": "愚人",
            "0-5-1": "劳动",
            "0-5-4": "青年",
            "0-6-1": "儿童",
            "0-9-10": "教师",
            "0-9-18": "国耻",
            "0-10-1": "国庆",
            "0-12-25": "圣诞"
        } : {}, a.mark), lay.each(["min", "max"], function (e, t) {
            var n = [], l = [];
            if ("number" == typeof a[t]) {
                var i = a[t], r = (new Date).getTime(), o = 864e5, s = new Date(i ? i < o ? r + i * o : i : r);
                n = [s.getFullYear(), s.getMonth() + 1, s.getDate()], i < o || (l = [s.getHours(), s.getMinutes(), s.getSeconds()])
            } else n = (a[t].match(/\d+-\d+-\d+/) || [""])[0].split("-"), l = (a[t].match(/\d+:\d+:\d+/) || [""])[0].split(":");
            a[t] = {
                year: 0 | n[0] || (new Date).getFullYear(),
                month: n[1] ? (0 | n[1]) - 1 : (new Date).getMonth(),
                date: 0 | n[2] || (new Date).getDate(),
                hours: 0 | l[0],
                minutes: 0 | l[1],
                seconds: 0 | l[2]
            }
        }), t.elemID = "layui-laydate" + a.elem.attr("lay-key"), (a.show || l) && t.render(), l || t.events(), a.value && a.isInitValue && (a.value.constructor === Date ? t.setValue(t.parse(0, t.systemDate(a.value))) : t.setValue(a.value)))
    }, v.prototype.render = function () {
        var e = this, t = e.config, a = e.lang(), n = "static" === t.position, l = e.elem = lay.elem("div", {
                id: e.elemID,
                "class": ["layui-laydate", t.range ? " layui-laydate-range" : "", n ? " " + d : "", t.theme && "default" !== t.theme && !/^#/.test(t.theme) ? " laydate-theme-" + t.theme : ""].join("")
            }), i = e.elemMain = [], r = e.elemHeader = [], o = e.elemCont = [], s = e.table = [],
            y = e.footer = lay.elem("div", {"class": h});
        if (t.zIndex && (l.style.zIndex = t.zIndex), lay.each(new Array(2), function (e) {
            if (!t.range && e > 0) return !0;
            var n = lay.elem("div", {"class": "layui-laydate-header"}), l = [function () {
                    var e = lay.elem("i", {"class": "layui-icon laydate-icon laydate-prev-y"});
                    return e.innerHTML = "&#xe65a;", e
                }(), function () {
                    var e = lay.elem("i", {"class": "layui-icon laydate-icon laydate-prev-m"});
                    return e.innerHTML = "&#xe603;", e
                }(), function () {
                    var e = lay.elem("div", {"class": "laydate-set-ym"}), t = lay.elem("span"), a = lay.elem("span");
                    return e.appendChild(t), e.appendChild(a), e
                }(), function () {
                    var e = lay.elem("i", {"class": "layui-icon laydate-icon laydate-next-m"});
                    return e.innerHTML = "&#xe602;", e
                }(), function () {
                    var e = lay.elem("i", {"class": "layui-icon laydate-icon laydate-next-y"});
                    return e.innerHTML = "&#xe65b;", e
                }()], y = lay.elem("div", {"class": "layui-laydate-content"}), d = lay.elem("table"), m = lay.elem("thead"),
                c = lay.elem("tr");
            lay.each(l, function (e, t) {
                n.appendChild(t)
            }), m.appendChild(c), lay.each(new Array(6), function (e) {
                var t = d.insertRow(0);
                lay.each(new Array(7), function (n) {
                    if (0 === e) {
                        var l = lay.elem("th");
                        l.innerHTML = a.weeks[n], c.appendChild(l)
                    }
                    t.insertCell(n)
                })
            }), d.insertBefore(m, d.children[0]), y.appendChild(d), i[e] = lay.elem("div", {"class": "layui-laydate-main laydate-main-list-" + e}), i[e].appendChild(n), i[e].appendChild(y), r.push(l), o.push(y), s.push(d)
        }), lay(y).html(function () {
            var e = [], l = [];
            return "datetime" === t.type && e.push('<span lay-type="datetime" class="laydate-btns-time">' + a.timeTips + "</span>"), lay.each(t.btns, function (e, i) {
                var r = a.tools[i] || "btn";
                t.range && "now" === i || (n && "clear" === i && (r = "cn" === t.lang ? "重置" : "Reset"), l.push('<span lay-type="' + i + '" class="laydate-btns-' + i + '">' + r + "</span>"))
            }), e.push('<div class="laydate-footer-btns">' + l.join("") + "</div>"), e.join("")
        }()), lay.each(i, function (e, t) {
            l.appendChild(t)
        }), t.showBottom && l.appendChild(y), /^#/.test(t.theme)) {
            var m = lay.elem("style"),
                c = ["#{{id}} .layui-laydate-header{background-color:{{theme}};}", "#{{id}} .layui-this{background-color:{{theme}} !important;}"].join("").replace(/{{id}}/g, e.elemID).replace(/{{theme}}/g, t.theme);
            "styleSheet" in m ? (m.setAttribute("type", "text/css"), m.styleSheet.cssText = c) : m.innerHTML = c, lay(l).addClass("laydate-theme-molv"), l.appendChild(m)
        }
        e.remove(v.thisElemDate), n ? t.elem.append(l) : (document.body.appendChild(l), e.position()), e.checkDate().calendar(null, 0, "init"), e.changeEvent(), v.thisElemDate = e.elemID, "function" == typeof t.ready && t.ready(lay.extend({}, t.dateTime, {month: t.dateTime.month + 1}))
    }, v.prototype.remove = function (e) {
        var t = this, a = (t.config, lay("#" + (e || t.elemID)));
        return a[0] ? (a.hasClass(d) || t.checkDate(function () {
            a.remove(), delete t.endDate
        }), t) : t
    }, v.prototype.position = function () {
        var e = this, t = e.config;
        return lay.position(e.bindElem || t.elem[0], e.elem, {position: t.position}), e
    }, v.prototype.hint = function (e) {
        var t = this, a = (t.config, lay.elem("div", {"class": u}));
        t.elem && (a.innerHTML = e || "", lay(t.elem).find("." + u).remove(), t.elem.appendChild(a), clearTimeout(t.hinTimer), t.hinTimer = setTimeout(function () {
            lay(t.elem).find("." + u).remove()
        }, 3e3))
    }, v.prototype.getAsYM = function (e, t, a) {
        return a ? t-- : t++, t < 0 && (t = 11, e--), t > 11 && (t = 0, e++), [e, t]
    }, v.prototype.systemDate = function (e) {
        var t = e || new Date;
        return {
            year: t.getFullYear(),
            month: t.getMonth(),
            date: t.getDate(),
            hours: e ? e.getHours() : 0,
            minutes: e ? e.getMinutes() : 0,
            seconds: e ? e.getSeconds() : 0
        }
    }, v.prototype.checkDate = function (e) {
        var t, a, l = this, i = (new Date, l.config), r = l.lang(), o = i.dateTime = i.dateTime || l.systemDate(),
            s = l.bindElem || i.elem[0],
            d = (l.isInput(s) ? "val" : "html", l.isInput(s) ? s.value : "static" === i.position ? "" : s.innerHTML),
            m = function (e) {
                e.year > y[1] && (e.year = y[1], a = !0), e.month > 11 && (e.month = 11, a = !0), e.hours > 23 && (e.hours = 0, a = !0), e.minutes > 59 && (e.minutes = 0, e.hours++, a = !0), e.seconds > 59 && (e.seconds = 0, e.minutes++, a = !0), t = n.getEndDate(e.month + 1, e.year), e.date > t && (e.date = t, a = !0)
            }, c = function (e, t, n) {
                var r = ["startTime", "endTime"];
                t = (t.match(l.EXP_SPLIT) || []).slice(1), n = n || 0, i.range && (l[r[n]] = l[r[n]] || {}), lay.each(l.format, function (o, s) {
                    var d = parseFloat(t[o]);
                    t[o].length < s.length && (a = !0), /yyyy|y/.test(s) ? (d < y[0] && (d = y[0], a = !0), e.year = d) : /MM|M/.test(s) ? (d < 1 && (d = 1, a = !0), e.month = d - 1) : /dd|d/.test(s) ? (d < 1 && (d = 1, a = !0), e.date = d) : /HH|H/.test(s) ? (d < 1 && (d = 0, a = !0), e.hours = d, i.range && (l[r[n]].hours = d)) : /mm|m/.test(s) ? (d < 1 && (d = 0, a = !0), e.minutes = d, i.range && (l[r[n]].minutes = d)) : /ss|s/.test(s) && (d < 1 && (d = 0, a = !0), e.seconds = d, i.range && (l[r[n]].seconds = d))
                }), m(e)
            };
        return "limit" === e ? (m(o), l) : (d = d || i.value, "string" == typeof d && (d = d.replace(/\s+/g, " ").replace(/^\s|\s$/g, "")), i.range && (l.endDate = l.endDate || lay.extend({}, o, function () {
            var e = {}, t = l.getAsYM(o.year, o.month);
            return "year" === i.type ? e.year = o.year + 1 : "time" !== i.type && (e.year = t[0], e.month = t[1]), e
        }())), "string" == typeof d && d ? l.EXP_IF.test(d) ? i.range ? (d = d.split(" " + i.range + " "), lay.each([i.dateTime, l.endDate], function (e, t) {
            c(t, d[e], e)
        })) : c(o, d) : (l.hint(r.formatError[0] + (i.range ? i.format + " " + i.range + " " + i.format : i.format) + r.formatError[1]), a = !0) : d && d.constructor === Date ? i.dateTime = l.systemDate(d) : (i.dateTime = l.systemDate(), delete l.startTime, delete l.endTime), m(o), a && d && l.setValue(i.range ? l.endDate ? l.parse() : "" : l.parse()), e && e(), l)
    }, v.prototype.mark = function (e, t) {
        var a, n = this, l = n.config;
        return lay.each(l.mark, function (e, n) {
            var l = e.split("-");
            l[0] != t[0] && 0 != l[0] || l[1] != t[1] && 0 != l[1] || l[2] != t[2] || (a = n || t[2])
        }), a && e.html('<span class="laydate-day-mark">' + a + "</span>"), n
    }, v.prototype.limit = function (e, t, a, n) {
        var l, i = this, r = i.config, o = {}, y = r[a > 41 ? "endDate" : "dateTime"], d = lay.extend({}, y, t || {});
        return lay.each({now: d, min: r.min, max: r.max}, function (e, t) {
            o[e] = i.newDate(lay.extend({year: t.year, month: t.month, date: t.date}, function () {
                var e = {};
                return lay.each(n, function (a, n) {
                    e[n] = t[n]
                }), e
            }())).getTime()
        }), l = o.now < o.min || o.now > o.max, e && e[l ? "addClass" : "removeClass"](s), l
    }, v.prototype.thisDateTime = function (e) {
        var t = this, a = t.config;
        return e ? t.endDate : a.dateTime
    }, v.prototype.calendar = function (e, t, a) {
        var l, i, r, s = this, d = s.config, t = t ? 1 : 0, m = e || s.thisDateTime(t), c = new Date, u = s.lang(),
            h = "date" !== d.type && "datetime" !== d.type, p = lay(s.table[t]).find("td"),
            g = lay(s.elemHeader[t][2]).find("span");
        return m.year < y[0] && (m.year = y[0], s.hint(u.invalidDate)), m.year > y[1] && (m.year = y[1], s.hint(u.invalidDate)), s.firstDate || (s.firstDate = lay.extend({}, m)), c.setFullYear(m.year, m.month, 1), l = c.getDay(), i = n.getEndDate(m.month || 12, m.year), r = n.getEndDate(m.month + 1, m.year), lay.each(p, function (e, t) {
            var a = [m.year, m.month], n = 0;
            t = lay(t), t.removeAttr("class"), e < l ? (n = i - l + e, t.addClass("laydate-day-prev"), a = s.getAsYM(m.year, m.month, "sub")) : e >= l && e < r + l ? (n = e - l, n + 1 === m.date && t.addClass(o)) : (n = e - r - l, t.addClass("laydate-day-next"), a = s.getAsYM(m.year, m.month)), a[1]++, a[2] = n + 1, t.attr("lay-ymd", a.join("-")).html(a[2]), s.mark(t, a).limit(t, {
                year: a[0],
                month: a[1] - 1,
                date: a[2]
            }, e)
        }), lay(g[0]).attr("lay-ym", m.year + "-" + (m.month + 1)), lay(g[1]).attr("lay-ym", m.year + "-" + (m.month + 1)), "cn" === d.lang ? (lay(g[0]).attr("lay-type", "year").html(m.year + "年"), lay(g[1]).attr("lay-type", "month").html(m.month + 1 + "月")) : (lay(g[0]).attr("lay-type", "month").html(u.month[m.month]), lay(g[1]).attr("lay-type", "year").html(m.year)), h && (d.range ? e && (s.listYM = [[d.dateTime.year, d.dateTime.month + 1], [s.endDate.year, s.endDate.month + 1]], s.list(d.type, 0).list(d.type, 1), "time" === d.type ? s.setBtnStatus("时间", lay.extend({}, s.systemDate(), s.startTime), lay.extend({}, s.systemDate(), s.endTime)) : s.setBtnStatus(!0)) : (s.listYM = [[m.year, m.month + 1]], s.list(d.type, 0))), d.range && "init" === a && !e && s.calendar(s.endDate, 1), d.range || s.limit(lay(s.footer).find(f), null, 0, ["hours", "minutes", "seconds"]), s.setBtnStatus(), s.stampRange(t, p), s
    }, v.prototype.list = function (e, t) {
        var a = this, n = a.config, l = n.dateTime, i = a.lang(),
            r = n.range && "date" !== n.type && "datetime" !== n.type, y = lay.elem("ul", {
                "class": m + " " + {
                    year: "laydate-year-list",
                    month: "laydate-month-list",
                    time: "laydate-time-list"
                }[e]
            }), d = a.elemHeader[t], c = lay(d[2]).find("span"), u = a.elemCont[t || 0], h = lay(u).find("." + m)[0],
            v = "cn" === n.lang, T = v ? "年" : "", D = a.listYM[t] || {}, M = ["hours", "minutes", "seconds"],
            x = ["startTime", "endTime"][t];
        if (D[0] < 1 && (D[0] = 1), "year" === e) {
            var C, w = C = D[0] - 7;
            w < 1 && (w = C = 1), lay.each(new Array(15), function (e) {
                var l = lay.elem("li", {"lay-ym": C}), i = {year: C};
                C == D[0] && lay(l).addClass(o), l.innerHTML = C + T, y.appendChild(l), C < a.firstDate.year ? (i.month = n.min.month, i.date = n.min.date) : C >= a.firstDate.year && (i.month = n.max.month, i.date = n.max.date), a.limit(lay(l), i, t), C++
            }), lay(c[v ? 0 : 1]).attr("lay-ym", C - 8 + "-" + D[1]).html(w + T + " - " + (C - 1 + T))
        } else if ("month" === e) lay.each(new Array(12), function (e) {
            var l = lay.elem("li", {"lay-ym": e}), r = {year: D[0], month: e};
            e + 1 == D[1] && lay(l).addClass(o), l.innerHTML = i.month[e] + (v ? "月" : ""), y.appendChild(l), D[0] < a.firstDate.year ? r.date = n.min.date : D[0] >= a.firstDate.year && (r.date = n.max.date), a.limit(lay(l), r, t)
        }), lay(c[v ? 0 : 1]).attr("lay-ym", D[0] + "-" + D[1]).html(D[0] + T); else if ("time" === e) {
            var k = function () {
                lay(y).find("ol").each(function (e, n) {
                    lay(n).find("li").each(function (n, l) {
                        a.limit(lay(l), [{hours: n}, {hours: a[x].hours, minutes: n}, {
                            hours: a[x].hours,
                            minutes: a[x].minutes,
                            seconds: n
                        }][e], t, [["hours"], ["hours", "minutes"], ["hours", "minutes", "seconds"]][e])
                    })
                }), n.range || a.limit(lay(a.footer).find(f), a[x], 0, ["hours", "minutes", "seconds"])
            };
            n.range ? a[x] || (a[x] = {
                hours: 0,
                minutes: 0,
                seconds: 0
            }) : a[x] = l, lay.each([24, 60, 60], function (e, t) {
                var n = lay.elem("li"), l = ["<p>" + i.time[e] + "</p><ol>"];
                lay.each(new Array(t), function (t) {
                    l.push("<li" + (a[x][M[e]] === t ? ' class="' + o + '"' : "") + ">" + lay.digit(t, 2) + "</li>")
                }), n.innerHTML = l.join("") + "</ol>", y.appendChild(n)
            }), k()
        }
        if (h && u.removeChild(h), u.appendChild(y), "year" === e || "month" === e) lay(a.elemMain[t]).addClass("laydate-ym-show"), lay(y).find("li").on("click", function () {
            var i = 0 | lay(this).attr("lay-ym");
            if (!lay(this).hasClass(s)) {
                0 === t ? (l[e] = i, a.limit(lay(a.footer).find(f), null, 0)) : a.endDate[e] = i;
                var d = "year" === n.type || "month" === n.type;
                d ? (lay(y).find("." + o).removeClass(o), lay(this).addClass(o), "month" === n.type && "year" === e && (a.listYM[t][0] = i, r && (t ? l.year = i : a.endDate.year = i), a.list("month", t))) : (a.checkDate("limit").calendar(null, t), a.closeList()), a.setBtnStatus(), n.range || (("month" === n.type && "month" === e || "year" === n.type && "year" === e) && a.setValue(a.parse()).remove().done(), a.done(null, "change")), lay(a.footer).find(g).removeClass(s)
            }
        }); else {
            var E = lay.elem("span", {"class": p}), b = function () {
                lay(y).find("ol").each(function (e) {
                    var t = this, n = lay(t).find("li");
                    t.scrollTop = 30 * (a[x][M[e]] - 2), t.scrollTop <= 0 && n.each(function (e, a) {
                        if (!lay(this).hasClass(s)) return t.scrollTop = 30 * (e - 2), !0
                    })
                })
            }, H = lay(d[2]).find("." + p);
            b(), E.innerHTML = n.range ? [i.startTime, i.endTime][t] : i.timeTips, lay(a.elemMain[t]).addClass("laydate-time-show"), H[0] && H.remove(), d[2].appendChild(E), lay(y).find("ol").each(function (e) {
                var t = this;
                lay(t).find("li").on("click", function () {
                    var i = 0 | this.innerHTML;
                    lay(this).hasClass(s) || (n.range ? a[x][M[e]] = i : l[M[e]] = i, lay(t).find("." + o).removeClass(o), lay(this).addClass(o), k(), b(), (a.endDate || "time" === n.type) && a.done(null, "change"), a.setBtnStatus())
                })
            })
        }
        return a
    }, v.prototype.listYM = [], v.prototype.closeList = function () {
        var e = this;
        e.config;
        lay.each(e.elemCont, function (t, a) {
            lay(this).find("." + m).remove(), lay(e.elemMain[t]).removeClass("laydate-ym-show laydate-time-show")
        }), lay(e.elem).find("." + p).remove()
    }, v.prototype.setBtnStatus = function (e, t, a) {
        var n, l = this, i = l.config, r = l.lang(), o = lay(l.footer).find(f);
        i.range && "time" !== i.type && (t = t || i.dateTime, a = a || l.endDate, n = l.newDate(t).getTime() > l.newDate(a).getTime(), l.limit(null, t) || l.limit(null, a) ? o.addClass(s) : o[n ? "addClass" : "removeClass"](s), e && n && l.hint("string" == typeof e ? r.timeout.replace(/日期/g, e) : r.timeout))
    }, v.prototype.parse = function (e, t) {
        var a = this, n = a.config,
            l = t || (e ? lay.extend({}, a.endDate, a.endTime) : n.range ? lay.extend({}, n.dateTime, a.startTime) : n.dateTime),
            i = a.format.concat();
        return lay.each(i, function (e, t) {
            /yyyy|y/.test(t) ? i[e] = lay.digit(l.year, t.length) : /MM|M/.test(t) ? i[e] = lay.digit(l.month + 1, t.length) : /dd|d/.test(t) ? i[e] = lay.digit(l.date, t.length) : /HH|H/.test(t) ? i[e] = lay.digit(l.hours, t.length) : /mm|m/.test(t) ? i[e] = lay.digit(l.minutes, t.length) : /ss|s/.test(t) && (i[e] = lay.digit(l.seconds, t.length))
        }), n.range && !e ? i.join("") + " " + n.range + " " + a.parse(1) : i.join("")
    }, v.prototype.newDate = function (e) {
        return e = e || {}, new Date(e.year || 1, e.month || 0, e.date || 1, e.hours || 0, e.minutes || 0, e.seconds || 0)
    }, v.prototype.setValue = function (e) {
        var t = this, a = t.config, n = t.bindElem || a.elem[0], l = t.isInput(n) ? "val" : "html";
        return "static" === a.position || lay(n)[l](e || ""), this
    }, v.prototype.stampRange = function (e, t) {
        var a, n, l = this, i = l.config;
        i.range && (a = l.newDate(i.dateTime).getTime(), n = l.newDate(l.endDate).getTime(), lay.each(t, function (t, i) {
            var r = lay(i).attr("lay-ymd").split("-"),
                o = l.newDate({year: r[0], month: r[1] - 1, date: r[2]}).getTime();
            0 == e ? o > a && lay(i).addClass(c) : o < n && lay(i).addClass(c)
        }))
    }, v.prototype.done = function (e, t) {
        var a = this, n = a.config, l = lay.extend({}, lay.extend(n.dateTime, a.startTime)),
            i = lay.extend({}, lay.extend(a.endDate, a.endTime));
        return lay.each([l, i], function (e, t) {
            "month" in t && lay.extend(t, {month: t.month + 1})
        }), e = e || [a.parse(), l, i], "function" == typeof n[t || "done"] && n[t || "done"].apply(n, e), a
    }, v.prototype.choose = function (e, t) {
        var a = this, n = a.config, l = a.thisDateTime(t), i = (lay(a.elem).find("td"), e.attr("lay-ymd").split("-"));
        i = {
            year: 0 | i[0],
            month: (0 | i[1]) - 1,
            date: 0 | i[2]
        }, e.hasClass(s) || (lay.extend(l, i), n.range ? (lay.each(["startTime", "endTime"], function (e, t) {
            a[t] = a[t] || {hours: 0, minutes: 0, seconds: 0}
        }), a.calendar(null, t)) : "static" === n.position ? a.calendar().done().done(null, "change") : "date" === n.type ? a.setValue(a.parse()).remove().done() : "datetime" === n.type && a.calendar().done(null, "change"))
    }, v.prototype.tool = function (e, t) {
        var a = this, n = a.config, l = a.lang(), i = n.dateTime, r = "static" === n.position, o = {
            datetime: function () {
                lay(e).hasClass(s) || (a.list("time", 0), n.range && a.list("time", 1), lay(e).attr("lay-type", "date").html(a.lang().dateTips))
            }, date: function () {
                a.closeList(), lay(e).attr("lay-type", "datetime").html(a.lang().timeTips)
            }, clear: function () {
                a.setValue("").remove(), r && (lay.extend(i, a.firstDate), a.calendar()), n.range && (delete a.endDate, delete a.startTime, delete a.endTime), a.done(["", {}, {}])
            }, now: function () {
                var e = new Date;
                lay.extend(i, a.systemDate(), {
                    hours: e.getHours(),
                    minutes: e.getMinutes(),
                    seconds: e.getSeconds()
                }), a.setValue(a.parse()).remove(), r && a.calendar(), a.done()
            }, confirm: function () {
                if (n.range) {
                    if (lay(e).hasClass(s)) return a.hint("time" === n.type ? l.timeout.replace(/日期/g, "时间") : l.timeout)
                } else if (lay(e).hasClass(s)) return a.hint(l.invalidDate);
                a.done(), a.setValue(a.parse()).remove()
            }
        };
        o[t] && o[t]()
    }, v.prototype.change = function (e) {
        var t = this, a = t.config, n = t.thisDateTime(e), l = a.range && ("year" === a.type || "month" === a.type),
            i = t.elemCont[e || 0], r = t.listYM[e], o = function (o) {
                var s = lay(i).find(".laydate-year-list")[0], y = lay(i).find(".laydate-month-list")[0];
                return s && (r[0] = o ? r[0] - 15 : r[0] + 15, t.list("year", e)), y && (o ? r[0]-- : r[0]++, t.list("month", e)), (s || y) && (lay.extend(n, {year: r[0]}), l && (n.year = r[0]), a.range || t.done(null, "change"), a.range || t.limit(lay(t.footer).find(f), {year: r[0]})), t.setBtnStatus(), s || y
            };
        return {
            prevYear: function () {
                o("sub") || (n.year--, t.checkDate("limit").calendar(null, e), a.range || t.done(null, "change"))
            }, prevMonth: function () {
                var l = t.getAsYM(n.year, n.month, "sub");
                lay.extend(n, {
                    year: l[0],
                    month: l[1]
                }), t.checkDate("limit").calendar(null, e), a.range || t.done(null, "change")
            }, nextMonth: function () {
                var l = t.getAsYM(n.year, n.month);
                lay.extend(n, {
                    year: l[0],
                    month: l[1]
                }), t.checkDate("limit").calendar(null, e), a.range || t.done(null, "change")
            }, nextYear: function () {
                o() || (n.year++, t.checkDate("limit").calendar(null, e), a.range || t.done(null, "change"))
            }
        }
    }, v.prototype.changeEvent = function () {
        var e = this;
        e.config;
        lay(e.elem).on("click", function (e) {
            lay.stope(e)
        }), lay.each(e.elemHeader, function (t, a) {
            lay(a[0]).on("click", function (a) {
                e.change(t).prevYear()
            }), lay(a[1]).on("click", function (a) {
                e.change(t).prevMonth()
            }), lay(a[2]).find("span").on("click", function (a) {
                var n = lay(this), l = n.attr("lay-ym"), i = n.attr("lay-type");
                l && (l = l.split("-"), e.listYM[t] = [0 | l[0], 0 | l[1]], e.list(i, t), lay(e.footer).find(g).addClass(s))
            }), lay(a[3]).on("click", function (a) {
                e.change(t).nextMonth()
            }), lay(a[4]).on("click", function (a) {
                e.change(t).nextYear()
            })
        }), lay.each(e.table, function (t, a) {
            var n = lay(a).find("td");
            n.on("click", function () {
                e.choose(lay(this), t)
            })
        }), lay(e.footer).find("span").on("click", function () {
            var t = lay(this).attr("lay-type");
            e.tool(this, t)
        })
    }, v.prototype.isInput = function (e) {
        return /input|textarea/.test(e.tagName.toLocaleLowerCase())
    }, v.prototype.events = function () {
        var t = this, a = t.config, n = function (e, n) {
            e.on(a.trigger, function () {
                n && (t.bindElem = this), t.render()
            })
        };
        a.elem[0] && !a.elem[0].eventHandler && (n(a.elem, "bind"), n(a.eventElem), lay(document).on("click", function (e) {
            e.target !== a.elem[0] && e.target !== a.eventElem[0] && e.target !== lay(a.closeStop)[0] && t.remove()
        }).on("keydown", function (e) {
            13 === e.keyCode && lay("#" + t.elemID)[0] && t.elemID === v.thisElemDate && (e.preventDefault(), lay(t.footer).find(f)[0].click())
        }), lay(e).on("resize", function () {
            return !(!t.elem || !lay(r)[0]) && void t.position()
        }), a.elem[0].eventHandler = !0)
    }, n.render = function (e) {
        var t = new v(e);
        return l.call(t)
    }, n.getEndDate = function (e, t) {
        var a = new Date;
        return a.setFullYear(t || a.getFullYear(), e || a.getMonth() + 1, 1), new Date(a.getTime() - 864e5).getDate()
    }, t ? (n.ready(), layui.define("lay", function (e) {
        n.path = layui.cache.dir, e(i, n)
    })) : "function" == typeof define && define.amd ? define(function () {
        return n
    }) : function () {
        n.ready(), e.laydate = n
    }()
}(window);
!function (e, t) {
    "object" == typeof module && "object" == typeof module.exports ? module.exports = e.document ? t(e, !0) : function (e) {
        if (!e.document) throw new Error("jQuery requires a window with a document");
        return t(e)
    } : t(e)
}("undefined" != typeof window ? window : this, function (e, t) {
    function n(e) {
        var t = !!e && "length" in e && e.length, n = pe.type(e);
        return "function" !== n && !pe.isWindow(e) && ("array" === n || 0 === t || "number" == typeof t && t > 0 && t - 1 in e)
    }

    function r(e, t, n) {
        if (pe.isFunction(t)) return pe.grep(e, function (e, r) {
            return !!t.call(e, r, e) !== n
        });
        if (t.nodeType) return pe.grep(e, function (e) {
            return e === t !== n
        });
        if ("string" == typeof t) {
            if (Ce.test(t)) return pe.filter(t, e, n);
            t = pe.filter(t, e)
        }
        return pe.grep(e, function (e) {
            return pe.inArray(e, t) > -1 !== n
        })
    }

    function i(e, t) {
        do e = e[t]; while (e && 1 !== e.nodeType);
        return e
    }

    function o(e) {
        var t = {};
        return pe.each(e.match(De) || [], function (e, n) {
            t[n] = !0
        }), t
    }

    function a() {
        re.addEventListener ? (re.removeEventListener("DOMContentLoaded", s), e.removeEventListener("load", s)) : (re.detachEvent("onreadystatechange", s), e.detachEvent("onload", s))
    }

    function s() {
        (re.addEventListener || "load" === e.event.type || "complete" === re.readyState) && (a(), pe.ready())
    }

    function u(e, t, n) {
        if (void 0 === n && 1 === e.nodeType) {
            var r = "data-" + t.replace(_e, "-$1").toLowerCase();
            if (n = e.getAttribute(r), "string" == typeof n) {
                try {
                    n = "true" === n || "false" !== n && ("null" === n ? null : +n + "" === n ? +n : qe.test(n) ? pe.parseJSON(n) : n)
                } catch (i) {
                }
                pe.data(e, t, n)
            } else n = void 0
        }
        return n
    }

    function l(e) {
        var t;
        for (t in e) if (("data" !== t || !pe.isEmptyObject(e[t])) && "toJSON" !== t) return !1;
        return !0
    }

    function c(e, t, n, r) {
        if (He(e)) {
            var i, o, a = pe.expando, s = e.nodeType, u = s ? pe.cache : e, l = s ? e[a] : e[a] && a;
            if (l && u[l] && (r || u[l].data) || void 0 !== n || "string" != typeof t) return l || (l = s ? e[a] = ne.pop() || pe.guid++ : a), u[l] || (u[l] = s ? {} : {toJSON: pe.noop}), "object" != typeof t && "function" != typeof t || (r ? u[l] = pe.extend(u[l], t) : u[l].data = pe.extend(u[l].data, t)), o = u[l], r || (o.data || (o.data = {}), o = o.data), void 0 !== n && (o[pe.camelCase(t)] = n), "string" == typeof t ? (i = o[t], null == i && (i = o[pe.camelCase(t)])) : i = o, i
        }
    }

    function f(e, t, n) {
        if (He(e)) {
            var r, i, o = e.nodeType, a = o ? pe.cache : e, s = o ? e[pe.expando] : pe.expando;
            if (a[s]) {
                if (t && (r = n ? a[s] : a[s].data)) {
                    pe.isArray(t) ? t = t.concat(pe.map(t, pe.camelCase)) : t in r ? t = [t] : (t = pe.camelCase(t), t = t in r ? [t] : t.split(" ")), i = t.length;
                    for (; i--;) delete r[t[i]];
                    if (n ? !l(r) : !pe.isEmptyObject(r)) return
                }
                (n || (delete a[s].data, l(a[s]))) && (o ? pe.cleanData([e], !0) : fe.deleteExpando || a != a.window ? delete a[s] : a[s] = void 0)
            }
        }
    }

    function d(e, t, n, r) {
        var i, o = 1, a = 20, s = r ? function () {
                return r.cur()
            } : function () {
                return pe.css(e, t, "")
            }, u = s(), l = n && n[3] || (pe.cssNumber[t] ? "" : "px"),
            c = (pe.cssNumber[t] || "px" !== l && +u) && Me.exec(pe.css(e, t));
        if (c && c[3] !== l) {
            l = l || c[3], n = n || [], c = +u || 1;
            do o = o || ".5", c /= o, pe.style(e, t, c + l); while (o !== (o = s() / u) && 1 !== o && --a)
        }
        return n && (c = +c || +u || 0, i = n[1] ? c + (n[1] + 1) * n[2] : +n[2], r && (r.unit = l, r.start = c, r.end = i)), i
    }

    function p(e) {
        var t = ze.split("|"), n = e.createDocumentFragment();
        if (n.createElement) for (; t.length;) n.createElement(t.pop());
        return n
    }

    function h(e, t) {
        var n, r, i = 0,
            o = "undefined" != typeof e.getElementsByTagName ? e.getElementsByTagName(t || "*") : "undefined" != typeof e.querySelectorAll ? e.querySelectorAll(t || "*") : void 0;
        if (!o) for (o = [], n = e.childNodes || e; null != (r = n[i]); i++) !t || pe.nodeName(r, t) ? o.push(r) : pe.merge(o, h(r, t));
        return void 0 === t || t && pe.nodeName(e, t) ? pe.merge([e], o) : o
    }

    function g(e, t) {
        for (var n, r = 0; null != (n = e[r]); r++) pe._data(n, "globalEval", !t || pe._data(t[r], "globalEval"))
    }

    function m(e) {
        Be.test(e.type) && (e.defaultChecked = e.checked)
    }

    function y(e, t, n, r, i) {
        for (var o, a, s, u, l, c, f, d = e.length, y = p(t), v = [], x = 0; x < d; x++) if (a = e[x], a || 0 === a) if ("object" === pe.type(a)) pe.merge(v, a.nodeType ? [a] : a); else if (Ue.test(a)) {
            for (u = u || y.appendChild(t.createElement("div")), l = (We.exec(a) || ["", ""])[1].toLowerCase(), f = Xe[l] || Xe._default, u.innerHTML = f[1] + pe.htmlPrefilter(a) + f[2], o = f[0]; o--;) u = u.lastChild;
            if (!fe.leadingWhitespace && $e.test(a) && v.push(t.createTextNode($e.exec(a)[0])), !fe.tbody) for (a = "table" !== l || Ve.test(a) ? "<table>" !== f[1] || Ve.test(a) ? 0 : u : u.firstChild, o = a && a.childNodes.length; o--;) pe.nodeName(c = a.childNodes[o], "tbody") && !c.childNodes.length && a.removeChild(c);
            for (pe.merge(v, u.childNodes), u.textContent = ""; u.firstChild;) u.removeChild(u.firstChild);
            u = y.lastChild
        } else v.push(t.createTextNode(a));
        for (u && y.removeChild(u), fe.appendChecked || pe.grep(h(v, "input"), m), x = 0; a = v[x++];) if (r && pe.inArray(a, r) > -1) i && i.push(a); else if (s = pe.contains(a.ownerDocument, a), u = h(y.appendChild(a), "script"), s && g(u), n) for (o = 0; a = u[o++];) Ie.test(a.type || "") && n.push(a);
        return u = null, y
    }

    function v() {
        return !0
    }

    function x() {
        return !1
    }

    function b() {
        try {
            return re.activeElement
        } catch (e) {
        }
    }

    function w(e, t, n, r, i, o) {
        var a, s;
        if ("object" == typeof t) {
            "string" != typeof n && (r = r || n, n = void 0);
            for (s in t) w(e, s, n, r, t[s], o);
            return e
        }
        if (null == r && null == i ? (i = n, r = n = void 0) : null == i && ("string" == typeof n ? (i = r, r = void 0) : (i = r, r = n, n = void 0)), i === !1) i = x; else if (!i) return e;
        return 1 === o && (a = i, i = function (e) {
            return pe().off(e), a.apply(this, arguments)
        }, i.guid = a.guid || (a.guid = pe.guid++)), e.each(function () {
            pe.event.add(this, t, i, r, n)
        })
    }

    function T(e, t) {
        return pe.nodeName(e, "table") && pe.nodeName(11 !== t.nodeType ? t : t.firstChild, "tr") ? e.getElementsByTagName("tbody")[0] || e.appendChild(e.ownerDocument.createElement("tbody")) : e
    }

    function C(e) {
        return e.type = (null !== pe.find.attr(e, "type")) + "/" + e.type, e
    }

    function E(e) {
        var t = it.exec(e.type);
        return t ? e.type = t[1] : e.removeAttribute("type"), e
    }

    function N(e, t) {
        if (1 === t.nodeType && pe.hasData(e)) {
            var n, r, i, o = pe._data(e), a = pe._data(t, o), s = o.events;
            if (s) {
                delete a.handle, a.events = {};
                for (n in s) for (r = 0, i = s[n].length; r < i; r++) pe.event.add(t, n, s[n][r])
            }
            a.data && (a.data = pe.extend({}, a.data))
        }
    }

    function k(e, t) {
        var n, r, i;
        if (1 === t.nodeType) {
            if (n = t.nodeName.toLowerCase(), !fe.noCloneEvent && t[pe.expando]) {
                i = pe._data(t);
                for (r in i.events) pe.removeEvent(t, r, i.handle);
                t.removeAttribute(pe.expando)
            }
            "script" === n && t.text !== e.text ? (C(t).text = e.text, E(t)) : "object" === n ? (t.parentNode && (t.outerHTML = e.outerHTML), fe.html5Clone && e.innerHTML && !pe.trim(t.innerHTML) && (t.innerHTML = e.innerHTML)) : "input" === n && Be.test(e.type) ? (t.defaultChecked = t.checked = e.checked, t.value !== e.value && (t.value = e.value)) : "option" === n ? t.defaultSelected = t.selected = e.defaultSelected : "input" !== n && "textarea" !== n || (t.defaultValue = e.defaultValue)
        }
    }

    function S(e, t, n, r) {
        t = oe.apply([], t);
        var i, o, a, s, u, l, c = 0, f = e.length, d = f - 1, p = t[0], g = pe.isFunction(p);
        if (g || f > 1 && "string" == typeof p && !fe.checkClone && rt.test(p)) return e.each(function (i) {
            var o = e.eq(i);
            g && (t[0] = p.call(this, i, o.html())), S(o, t, n, r)
        });
        if (f && (l = y(t, e[0].ownerDocument, !1, e, r), i = l.firstChild, 1 === l.childNodes.length && (l = i), i || r)) {
            for (s = pe.map(h(l, "script"), C), a = s.length; c < f; c++) o = l, c !== d && (o = pe.clone(o, !0, !0), a && pe.merge(s, h(o, "script"))), n.call(e[c], o, c);
            if (a) for (u = s[s.length - 1].ownerDocument, pe.map(s, E), c = 0; c < a; c++) o = s[c], Ie.test(o.type || "") && !pe._data(o, "globalEval") && pe.contains(u, o) && (o.src ? pe._evalUrl && pe._evalUrl(o.src) : pe.globalEval((o.text || o.textContent || o.innerHTML || "").replace(ot, "")));
            l = i = null
        }
        return e
    }

    function A(e, t, n) {
        for (var r, i = t ? pe.filter(t, e) : e, o = 0; null != (r = i[o]); o++) n || 1 !== r.nodeType || pe.cleanData(h(r)), r.parentNode && (n && pe.contains(r.ownerDocument, r) && g(h(r, "script")), r.parentNode.removeChild(r));
        return e
    }

    function D(e, t) {
        var n = pe(t.createElement(e)).appendTo(t.body), r = pe.css(n[0], "display");
        return n.detach(), r
    }

    function j(e) {
        var t = re, n = lt[e];
        return n || (n = D(e, t), "none" !== n && n || (ut = (ut || pe("<iframe frameborder='0' width='0' height='0'/>")).appendTo(t.documentElement), t = (ut[0].contentWindow || ut[0].contentDocument).document, t.write(), t.close(), n = D(e, t), ut.detach()), lt[e] = n), n
    }

    function L(e, t) {
        return {
            get: function () {
                return e() ? void delete this.get : (this.get = t).apply(this, arguments)
            }
        }
    }

    function H(e) {
        if (e in Et) return e;
        for (var t = e.charAt(0).toUpperCase() + e.slice(1), n = Ct.length; n--;) if (e = Ct[n] + t, e in Et) return e
    }

    function q(e, t) {
        for (var n, r, i, o = [], a = 0, s = e.length; a < s; a++) r = e[a], r.style && (o[a] = pe._data(r, "olddisplay"), n = r.style.display, t ? (o[a] || "none" !== n || (r.style.display = ""), "" === r.style.display && Re(r) && (o[a] = pe._data(r, "olddisplay", j(r.nodeName)))) : (i = Re(r), (n && "none" !== n || !i) && pe._data(r, "olddisplay", i ? n : pe.css(r, "display"))));
        for (a = 0; a < s; a++) r = e[a], r.style && (t && "none" !== r.style.display && "" !== r.style.display || (r.style.display = t ? o[a] || "" : "none"));
        return e
    }

    function _(e, t, n) {
        var r = bt.exec(t);
        return r ? Math.max(0, r[1] - (n || 0)) + (r[2] || "px") : t
    }

    function F(e, t, n, r, i) {
        for (var o = n === (r ? "border" : "content") ? 4 : "width" === t ? 1 : 0, a = 0; o < 4; o += 2) "margin" === n && (a += pe.css(e, n + Oe[o], !0, i)), r ? ("content" === n && (a -= pe.css(e, "padding" + Oe[o], !0, i)), "margin" !== n && (a -= pe.css(e, "border" + Oe[o] + "Width", !0, i))) : (a += pe.css(e, "padding" + Oe[o], !0, i), "padding" !== n && (a += pe.css(e, "border" + Oe[o] + "Width", !0, i)));
        return a
    }

    function M(e, t, n) {
        var r = !0, i = "width" === t ? e.offsetWidth : e.offsetHeight, o = ht(e),
            a = fe.boxSizing && "border-box" === pe.css(e, "boxSizing", !1, o);
        if (i <= 0 || null == i) {
            if (i = gt(e, t, o), (i < 0 || null == i) && (i = e.style[t]), ft.test(i)) return i;
            r = a && (fe.boxSizingReliable() || i === e.style[t]), i = parseFloat(i) || 0
        }
        return i + F(e, t, n || (a ? "border" : "content"), r, o) + "px"
    }

    function O(e, t, n, r, i) {
        return new O.prototype.init(e, t, n, r, i)
    }

    function R() {
        return e.setTimeout(function () {
            Nt = void 0
        }), Nt = pe.now()
    }

    function P(e, t) {
        var n, r = {height: e}, i = 0;
        for (t = t ? 1 : 0; i < 4; i += 2 - t) n = Oe[i], r["margin" + n] = r["padding" + n] = e;
        return t && (r.opacity = r.width = e), r
    }

    function B(e, t, n) {
        for (var r, i = ($.tweeners[t] || []).concat($.tweeners["*"]), o = 0, a = i.length; o < a; o++) if (r = i[o].call(n, t, e)) return r
    }

    function W(e, t, n) {
        var r, i, o, a, s, u, l, c, f = this, d = {}, p = e.style, h = e.nodeType && Re(e), g = pe._data(e, "fxshow");
        n.queue || (s = pe._queueHooks(e, "fx"), null == s.unqueued && (s.unqueued = 0, u = s.empty.fire, s.empty.fire = function () {
            s.unqueued || u()
        }), s.unqueued++, f.always(function () {
            f.always(function () {
                s.unqueued--, pe.queue(e, "fx").length || s.empty.fire()
            })
        })), 1 === e.nodeType && ("height" in t || "width" in t) && (n.overflow = [p.overflow, p.overflowX, p.overflowY], l = pe.css(e, "display"), c = "none" === l ? pe._data(e, "olddisplay") || j(e.nodeName) : l, "inline" === c && "none" === pe.css(e, "float") && (fe.inlineBlockNeedsLayout && "inline" !== j(e.nodeName) ? p.zoom = 1 : p.display = "inline-block")), n.overflow && (p.overflow = "hidden", fe.shrinkWrapBlocks() || f.always(function () {
            p.overflow = n.overflow[0], p.overflowX = n.overflow[1], p.overflowY = n.overflow[2]
        }));
        for (r in t) if (i = t[r], St.exec(i)) {
            if (delete t[r], o = o || "toggle" === i, i === (h ? "hide" : "show")) {
                if ("show" !== i || !g || void 0 === g[r]) continue;
                h = !0
            }
            d[r] = g && g[r] || pe.style(e, r)
        } else l = void 0;
        if (pe.isEmptyObject(d)) "inline" === ("none" === l ? j(e.nodeName) : l) && (p.display = l); else {
            g ? "hidden" in g && (h = g.hidden) : g = pe._data(e, "fxshow", {}), o && (g.hidden = !h), h ? pe(e).show() : f.done(function () {
                pe(e).hide()
            }), f.done(function () {
                var t;
                pe._removeData(e, "fxshow");
                for (t in d) pe.style(e, t, d[t])
            });
            for (r in d) a = B(h ? g[r] : 0, r, f), r in g || (g[r] = a.start, h && (a.end = a.start, a.start = "width" === r || "height" === r ? 1 : 0))
        }
    }

    function I(e, t) {
        var n, r, i, o, a;
        for (n in e) if (r = pe.camelCase(n), i = t[r], o = e[n], pe.isArray(o) && (i = o[1], o = e[n] = o[0]), n !== r && (e[r] = o, delete e[n]), a = pe.cssHooks[r], a && "expand" in a) {
            o = a.expand(o), delete e[r];
            for (n in o) n in e || (e[n] = o[n], t[n] = i)
        } else t[r] = i
    }

    function $(e, t, n) {
        var r, i, o = 0, a = $.prefilters.length, s = pe.Deferred().always(function () {
            delete u.elem
        }), u = function () {
            if (i) return !1;
            for (var t = Nt || R(), n = Math.max(0, l.startTime + l.duration - t), r = n / l.duration || 0, o = 1 - r, a = 0, u = l.tweens.length; a < u; a++) l.tweens[a].run(o);
            return s.notifyWith(e, [l, o, n]), o < 1 && u ? n : (s.resolveWith(e, [l]), !1)
        }, l = s.promise({
            elem: e,
            props: pe.extend({}, t),
            opts: pe.extend(!0, {specialEasing: {}, easing: pe.easing._default}, n),
            originalProperties: t,
            originalOptions: n,
            startTime: Nt || R(),
            duration: n.duration,
            tweens: [],
            createTween: function (t, n) {
                var r = pe.Tween(e, l.opts, t, n, l.opts.specialEasing[t] || l.opts.easing);
                return l.tweens.push(r), r
            },
            stop: function (t) {
                var n = 0, r = t ? l.tweens.length : 0;
                if (i) return this;
                for (i = !0; n < r; n++) l.tweens[n].run(1);
                return t ? (s.notifyWith(e, [l, 1, 0]), s.resolveWith(e, [l, t])) : s.rejectWith(e, [l, t]), this
            }
        }), c = l.props;
        for (I(c, l.opts.specialEasing); o < a; o++) if (r = $.prefilters[o].call(l, e, c, l.opts)) return pe.isFunction(r.stop) && (pe._queueHooks(l.elem, l.opts.queue).stop = pe.proxy(r.stop, r)), r;
        return pe.map(c, B, l), pe.isFunction(l.opts.start) && l.opts.start.call(e, l), pe.fx.timer(pe.extend(u, {
            elem: e,
            anim: l,
            queue: l.opts.queue
        })), l.progress(l.opts.progress).done(l.opts.done, l.opts.complete).fail(l.opts.fail).always(l.opts.always)
    }

    function z(e) {
        return pe.attr(e, "class") || ""
    }

    function X(e) {
        return function (t, n) {
            "string" != typeof t && (n = t, t = "*");
            var r, i = 0, o = t.toLowerCase().match(De) || [];
            if (pe.isFunction(n)) for (; r = o[i++];) "+" === r.charAt(0) ? (r = r.slice(1) || "*", (e[r] = e[r] || []).unshift(n)) : (e[r] = e[r] || []).push(n)
        }
    }

    function U(e, t, n, r) {
        function i(s) {
            var u;
            return o[s] = !0, pe.each(e[s] || [], function (e, s) {
                var l = s(t, n, r);
                return "string" != typeof l || a || o[l] ? a ? !(u = l) : void 0 : (t.dataTypes.unshift(l), i(l), !1)
            }), u
        }

        var o = {}, a = e === Qt;
        return i(t.dataTypes[0]) || !o["*"] && i("*")
    }

    function V(e, t) {
        var n, r, i = pe.ajaxSettings.flatOptions || {};
        for (r in t) void 0 !== t[r] && ((i[r] ? e : n || (n = {}))[r] = t[r]);
        return n && pe.extend(!0, e, n), e
    }

    function Y(e, t, n) {
        for (var r, i, o, a, s = e.contents, u = e.dataTypes; "*" === u[0];) u.shift(), void 0 === i && (i = e.mimeType || t.getResponseHeader("Content-Type"));
        if (i) for (a in s) if (s[a] && s[a].test(i)) {
            u.unshift(a);
            break
        }
        if (u[0] in n) o = u[0]; else {
            for (a in n) {
                if (!u[0] || e.converters[a + " " + u[0]]) {
                    o = a;
                    break
                }
                r || (r = a)
            }
            o = o || r
        }
        if (o) return o !== u[0] && u.unshift(o), n[o]
    }

    function J(e, t, n, r) {
        var i, o, a, s, u, l = {}, c = e.dataTypes.slice();
        if (c[1]) for (a in e.converters) l[a.toLowerCase()] = e.converters[a];
        for (o = c.shift(); o;) if (e.responseFields[o] && (n[e.responseFields[o]] = t), !u && r && e.dataFilter && (t = e.dataFilter(t, e.dataType)), u = o, o = c.shift()) if ("*" === o) o = u; else if ("*" !== u && u !== o) {
            if (a = l[u + " " + o] || l["* " + o], !a) for (i in l) if (s = i.split(" "), s[1] === o && (a = l[u + " " + s[0]] || l["* " + s[0]])) {
                a === !0 ? a = l[i] : l[i] !== !0 && (o = s[0], c.unshift(s[1]));
                break
            }
            if (a !== !0) if (a && e["throws"]) t = a(t); else try {
                t = a(t)
            } catch (f) {
                return {state: "parsererror", error: a ? f : "No conversion from " + u + " to " + o}
            }
        }
        return {state: "success", data: t}
    }

    function G(e) {
        return e.style && e.style.display || pe.css(e, "display")
    }

    function K(e) {
        if (!pe.contains(e.ownerDocument || re, e)) return !0;
        for (; e && 1 === e.nodeType;) {
            if ("none" === G(e) || "hidden" === e.type) return !0;
            e = e.parentNode
        }
        return !1
    }

    function Q(e, t, n, r) {
        var i;
        if (pe.isArray(t)) pe.each(t, function (t, i) {
            n || rn.test(e) ? r(e, i) : Q(e + "[" + ("object" == typeof i && null != i ? t : "") + "]", i, n, r)
        }); else if (n || "object" !== pe.type(t)) r(e, t); else for (i in t) Q(e + "[" + i + "]", t[i], n, r)
    }

    function Z() {
        try {
            return new e.XMLHttpRequest
        } catch (t) {
        }
    }

    function ee() {
        try {
            return new e.ActiveXObject("Microsoft.XMLHTTP")
        } catch (t) {
        }
    }

    function te(e) {
        return pe.isWindow(e) ? e : 9 === e.nodeType && (e.defaultView || e.parentWindow)
    }

    var ne = [], re = e.document, ie = ne.slice, oe = ne.concat, ae = ne.push, se = ne.indexOf, ue = {},
        le = ue.toString, ce = ue.hasOwnProperty, fe = {}, de = "1.12.4", pe = function (e, t) {
            return new pe.fn.init(e, t)
        }, he = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, ge = /^-ms-/, me = /-([\da-z])/gi, ye = function (e, t) {
            return t.toUpperCase()
        };
    pe.fn = pe.prototype = {
        jquery: de, constructor: pe, selector: "", length: 0, toArray: function () {
            return ie.call(this)
        }, get: function (e) {
            return null != e ? e < 0 ? this[e + this.length] : this[e] : ie.call(this)
        }, pushStack: function (e) {
            var t = pe.merge(this.constructor(), e);
            return t.prevObject = this, t.context = this.context, t
        }, each: function (e) {
            return pe.each(this, e)
        }, map: function (e) {
            return this.pushStack(pe.map(this, function (t, n) {
                return e.call(t, n, t)
            }))
        }, slice: function () {
            return this.pushStack(ie.apply(this, arguments))
        }, first: function () {
            return this.eq(0)
        }, last: function () {
            return this.eq(-1)
        }, eq: function (e) {
            var t = this.length, n = +e + (e < 0 ? t : 0);
            return this.pushStack(n >= 0 && n < t ? [this[n]] : [])
        }, end: function () {
            return this.prevObject || this.constructor()
        }, push: ae, sort: ne.sort, splice: ne.splice
    }, pe.extend = pe.fn.extend = function () {
        var e, t, n, r, i, o, a = arguments[0] || {}, s = 1, u = arguments.length, l = !1;
        for ("boolean" == typeof a && (l = a, a = arguments[s] || {}, s++), "object" == typeof a || pe.isFunction(a) || (a = {}), s === u && (a = this, s--); s < u; s++) if (null != (i = arguments[s])) for (r in i) e = a[r], n = i[r], a !== n && (l && n && (pe.isPlainObject(n) || (t = pe.isArray(n))) ? (t ? (t = !1, o = e && pe.isArray(e) ? e : []) : o = e && pe.isPlainObject(e) ? e : {}, a[r] = pe.extend(l, o, n)) : void 0 !== n && (a[r] = n));
        return a
    }, pe.extend({
        expando: "jQuery" + (de + Math.random()).replace(/\D/g, ""), isReady: !0, error: function (e) {
            throw new Error(e)
        }, noop: function () {
        }, isFunction: function (e) {
            return "function" === pe.type(e)
        }, isArray: Array.isArray || function (e) {
            return "array" === pe.type(e)
        }, isWindow: function (e) {
            return null != e && e == e.window
        }, isNumeric: function (e) {
            var t = e && e.toString();
            return !pe.isArray(e) && t - parseFloat(t) + 1 >= 0
        }, isEmptyObject: function (e) {
            var t;
            for (t in e) return !1;
            return !0
        }, isPlainObject: function (e) {
            var t;
            if (!e || "object" !== pe.type(e) || e.nodeType || pe.isWindow(e)) return !1;
            try {
                if (e.constructor && !ce.call(e, "constructor") && !ce.call(e.constructor.prototype, "isPrototypeOf")) return !1
            } catch (n) {
                return !1
            }
            if (!fe.ownFirst) for (t in e) return ce.call(e, t);
            for (t in e) ;
            return void 0 === t || ce.call(e, t)
        }, type: function (e) {
            return null == e ? e + "" : "object" == typeof e || "function" == typeof e ? ue[le.call(e)] || "object" : typeof e
        }, globalEval: function (t) {
            t && pe.trim(t) && (e.execScript || function (t) {
                e.eval.call(e, t)
            })(t)
        }, camelCase: function (e) {
            return e.replace(ge, "ms-").replace(me, ye)
        }, nodeName: function (e, t) {
            return e.nodeName && e.nodeName.toLowerCase() === t.toLowerCase()
        }, each: function (e, t) {
            var r, i = 0;
            if (n(e)) for (r = e.length; i < r && t.call(e[i], i, e[i]) !== !1; i++) ; else for (i in e) if (t.call(e[i], i, e[i]) === !1) break;
            return e
        }, trim: function (e) {
            return null == e ? "" : (e + "").replace(he, "")
        }, makeArray: function (e, t) {
            var r = t || [];
            return null != e && (n(Object(e)) ? pe.merge(r, "string" == typeof e ? [e] : e) : ae.call(r, e)), r
        }, inArray: function (e, t, n) {
            var r;
            if (t) {
                if (se) return se.call(t, e, n);
                for (r = t.length, n = n ? n < 0 ? Math.max(0, r + n) : n : 0; n < r; n++) if (n in t && t[n] === e) return n
            }
            return -1
        }, merge: function (e, t) {
            for (var n = +t.length, r = 0, i = e.length; r < n;) e[i++] = t[r++];
            if (n !== n) for (; void 0 !== t[r];) e[i++] = t[r++];
            return e.length = i, e
        }, grep: function (e, t, n) {
            for (var r, i = [], o = 0, a = e.length, s = !n; o < a; o++) r = !t(e[o], o), r !== s && i.push(e[o]);
            return i
        }, map: function (e, t, r) {
            var i, o, a = 0, s = [];
            if (n(e)) for (i = e.length; a < i; a++) o = t(e[a], a, r), null != o && s.push(o); else for (a in e) o = t(e[a], a, r), null != o && s.push(o);
            return oe.apply([], s)
        }, guid: 1, proxy: function (e, t) {
            var n, r, i;
            if ("string" == typeof t && (i = e[t], t = e, e = i), pe.isFunction(e)) return n = ie.call(arguments, 2), r = function () {
                return e.apply(t || this, n.concat(ie.call(arguments)))
            }, r.guid = e.guid = e.guid || pe.guid++, r
        }, now: function () {
            return +new Date
        }, support: fe
    }), "function" == typeof Symbol && (pe.fn[Symbol.iterator] = ne[Symbol.iterator]), pe.each("Boolean Number String Function Array Date RegExp Object Error Symbol".split(" "), function (e, t) {
        ue["[object " + t + "]"] = t.toLowerCase()
    });
    var ve = function (e) {
        function t(e, t, n, r) {
            var i, o, a, s, u, l, f, p, h = t && t.ownerDocument, g = t ? t.nodeType : 9;
            if (n = n || [], "string" != typeof e || !e || 1 !== g && 9 !== g && 11 !== g) return n;
            if (!r && ((t ? t.ownerDocument || t : B) !== H && L(t), t = t || H, _)) {
                if (11 !== g && (l = ye.exec(e))) if (i = l[1]) {
                    if (9 === g) {
                        if (!(a = t.getElementById(i))) return n;
                        if (a.id === i) return n.push(a), n
                    } else if (h && (a = h.getElementById(i)) && R(t, a) && a.id === i) return n.push(a), n
                } else {
                    if (l[2]) return Q.apply(n, t.getElementsByTagName(e)), n;
                    if ((i = l[3]) && w.getElementsByClassName && t.getElementsByClassName) return Q.apply(n, t.getElementsByClassName(i)), n
                }
                if (w.qsa && !X[e + " "] && (!F || !F.test(e))) {
                    if (1 !== g) h = t, p = e; else if ("object" !== t.nodeName.toLowerCase()) {
                        for ((s = t.getAttribute("id")) ? s = s.replace(xe, "\\$&") : t.setAttribute("id", s = P), f = N(e), o = f.length, u = de.test(s) ? "#" + s : "[id='" + s + "']"; o--;) f[o] = u + " " + d(f[o]);
                        p = f.join(","), h = ve.test(e) && c(t.parentNode) || t
                    }
                    if (p) try {
                        return Q.apply(n, h.querySelectorAll(p)), n
                    } catch (m) {
                    } finally {
                        s === P && t.removeAttribute("id")
                    }
                }
            }
            return S(e.replace(se, "$1"), t, n, r)
        }

        function n() {
            function e(n, r) {
                return t.push(n + " ") > T.cacheLength && delete e[t.shift()], e[n + " "] = r
            }

            var t = [];
            return e
        }

        function r(e) {
            return e[P] = !0, e
        }

        function i(e) {
            var t = H.createElement("div");
            try {
                return !!e(t)
            } catch (n) {
                return !1
            } finally {
                t.parentNode && t.parentNode.removeChild(t), t = null
            }
        }

        function o(e, t) {
            for (var n = e.split("|"), r = n.length; r--;) T.attrHandle[n[r]] = t
        }

        function a(e, t) {
            var n = t && e,
                r = n && 1 === e.nodeType && 1 === t.nodeType && (~t.sourceIndex || V) - (~e.sourceIndex || V);
            if (r) return r;
            if (n) for (; n = n.nextSibling;) if (n === t) return -1;
            return e ? 1 : -1
        }

        function s(e) {
            return function (t) {
                var n = t.nodeName.toLowerCase();
                return "input" === n && t.type === e
            }
        }

        function u(e) {
            return function (t) {
                var n = t.nodeName.toLowerCase();
                return ("input" === n || "button" === n) && t.type === e
            }
        }

        function l(e) {
            return r(function (t) {
                return t = +t, r(function (n, r) {
                    for (var i, o = e([], n.length, t), a = o.length; a--;) n[i = o[a]] && (n[i] = !(r[i] = n[i]))
                })
            })
        }

        function c(e) {
            return e && "undefined" != typeof e.getElementsByTagName && e
        }

        function f() {
        }

        function d(e) {
            for (var t = 0, n = e.length, r = ""; t < n; t++) r += e[t].value;
            return r
        }

        function p(e, t, n) {
            var r = t.dir, i = n && "parentNode" === r, o = I++;
            return t.first ? function (t, n, o) {
                for (; t = t[r];) if (1 === t.nodeType || i) return e(t, n, o)
            } : function (t, n, a) {
                var s, u, l, c = [W, o];
                if (a) {
                    for (; t = t[r];) if ((1 === t.nodeType || i) && e(t, n, a)) return !0
                } else for (; t = t[r];) if (1 === t.nodeType || i) {
                    if (l = t[P] || (t[P] = {}), u = l[t.uniqueID] || (l[t.uniqueID] = {}), (s = u[r]) && s[0] === W && s[1] === o) return c[2] = s[2];
                    if (u[r] = c, c[2] = e(t, n, a)) return !0
                }
            }
        }

        function h(e) {
            return e.length > 1 ? function (t, n, r) {
                for (var i = e.length; i--;) if (!e[i](t, n, r)) return !1;
                return !0
            } : e[0]
        }

        function g(e, n, r) {
            for (var i = 0, o = n.length; i < o; i++) t(e, n[i], r);
            return r
        }

        function m(e, t, n, r, i) {
            for (var o, a = [], s = 0, u = e.length, l = null != t; s < u; s++) (o = e[s]) && (n && !n(o, r, i) || (a.push(o), l && t.push(s)));
            return a
        }

        function y(e, t, n, i, o, a) {
            return i && !i[P] && (i = y(i)), o && !o[P] && (o = y(o, a)), r(function (r, a, s, u) {
                var l, c, f, d = [], p = [], h = a.length, y = r || g(t || "*", s.nodeType ? [s] : s, []),
                    v = !e || !r && t ? y : m(y, d, e, s, u), x = n ? o || (r ? e : h || i) ? [] : a : v;
                if (n && n(v, x, s, u), i) for (l = m(x, p), i(l, [], s, u), c = l.length; c--;) (f = l[c]) && (x[p[c]] = !(v[p[c]] = f));
                if (r) {
                    if (o || e) {
                        if (o) {
                            for (l = [], c = x.length; c--;) (f = x[c]) && l.push(v[c] = f);
                            o(null, x = [], l, u)
                        }
                        for (c = x.length; c--;) (f = x[c]) && (l = o ? ee(r, f) : d[c]) > -1 && (r[l] = !(a[l] = f))
                    }
                } else x = m(x === a ? x.splice(h, x.length) : x), o ? o(null, a, x, u) : Q.apply(a, x)
            })
        }

        function v(e) {
            for (var t, n, r, i = e.length, o = T.relative[e[0].type], a = o || T.relative[" "], s = o ? 1 : 0, u = p(function (e) {
                return e === t
            }, a, !0), l = p(function (e) {
                return ee(t, e) > -1
            }, a, !0), c = [function (e, n, r) {
                var i = !o && (r || n !== A) || ((t = n).nodeType ? u(e, n, r) : l(e, n, r));
                return t = null, i
            }]; s < i; s++) if (n = T.relative[e[s].type]) c = [p(h(c), n)]; else {
                if (n = T.filter[e[s].type].apply(null, e[s].matches), n[P]) {
                    for (r = ++s; r < i && !T.relative[e[r].type]; r++) ;
                    return y(s > 1 && h(c), s > 1 && d(e.slice(0, s - 1).concat({value: " " === e[s - 2].type ? "*" : ""})).replace(se, "$1"), n, s < r && v(e.slice(s, r)), r < i && v(e = e.slice(r)), r < i && d(e))
                }
                c.push(n)
            }
            return h(c)
        }

        function x(e, n) {
            var i = n.length > 0, o = e.length > 0, a = function (r, a, s, u, l) {
                var c, f, d, p = 0, h = "0", g = r && [], y = [], v = A, x = r || o && T.find.TAG("*", l),
                    b = W += null == v ? 1 : Math.random() || .1, w = x.length;
                for (l && (A = a === H || a || l); h !== w && null != (c = x[h]); h++) {
                    if (o && c) {
                        for (f = 0, a || c.ownerDocument === H || (L(c), s = !_); d = e[f++];) if (d(c, a || H, s)) {
                            u.push(c);
                            break
                        }
                        l && (W = b)
                    }
                    i && ((c = !d && c) && p--, r && g.push(c))
                }
                if (p += h, i && h !== p) {
                    for (f = 0; d = n[f++];) d(g, y, a, s);
                    if (r) {
                        if (p > 0) for (; h--;) g[h] || y[h] || (y[h] = G.call(u));
                        y = m(y)
                    }
                    Q.apply(u, y), l && !r && y.length > 0 && p + n.length > 1 && t.uniqueSort(u)
                }
                return l && (W = b, A = v), g
            };
            return i ? r(a) : a
        }

        var b, w, T, C, E, N, k, S, A, D, j, L, H, q, _, F, M, O, R, P = "sizzle" + 1 * new Date, B = e.document, W = 0,
            I = 0, $ = n(), z = n(), X = n(), U = function (e, t) {
                return e === t && (j = !0), 0
            }, V = 1 << 31, Y = {}.hasOwnProperty, J = [], G = J.pop, K = J.push, Q = J.push, Z = J.slice,
            ee = function (e, t) {
                for (var n = 0, r = e.length; n < r; n++) if (e[n] === t) return n;
                return -1
            },
            te = "checked|selected|async|autofocus|autoplay|controls|defer|disabled|hidden|ismap|loop|multiple|open|readonly|required|scoped",
            ne = "[\\x20\\t\\r\\n\\f]", re = "(?:\\\\.|[\\w-]|[^\\x00-\\xa0])+",
            ie = "\\[" + ne + "*(" + re + ")(?:" + ne + "*([*^$|!~]?=)" + ne + "*(?:'((?:\\\\.|[^\\\\'])*)'|\"((?:\\\\.|[^\\\\\"])*)\"|(" + re + "))|)" + ne + "*\\]",
            oe = ":(" + re + ")(?:\\((('((?:\\\\.|[^\\\\'])*)'|\"((?:\\\\.|[^\\\\\"])*)\")|((?:\\\\.|[^\\\\()[\\]]|" + ie + ")*)|.*)\\)|)",
            ae = new RegExp(ne + "+", "g"), se = new RegExp("^" + ne + "+|((?:^|[^\\\\])(?:\\\\.)*)" + ne + "+$", "g"),
            ue = new RegExp("^" + ne + "*," + ne + "*"), le = new RegExp("^" + ne + "*([>+~]|" + ne + ")" + ne + "*"),
            ce = new RegExp("=" + ne + "*([^\\]'\"]*?)" + ne + "*\\]", "g"), fe = new RegExp(oe),
            de = new RegExp("^" + re + "$"), pe = {
                ID: new RegExp("^#(" + re + ")"),
                CLASS: new RegExp("^\\.(" + re + ")"),
                TAG: new RegExp("^(" + re + "|[*])"),
                ATTR: new RegExp("^" + ie),
                PSEUDO: new RegExp("^" + oe),
                CHILD: new RegExp("^:(only|first|last|nth|nth-last)-(child|of-type)(?:\\(" + ne + "*(even|odd|(([+-]|)(\\d*)n|)" + ne + "*(?:([+-]|)" + ne + "*(\\d+)|))" + ne + "*\\)|)", "i"),
                bool: new RegExp("^(?:" + te + ")$", "i"),
                needsContext: new RegExp("^" + ne + "*[>+~]|:(even|odd|eq|gt|lt|nth|first|last)(?:\\(" + ne + "*((?:-\\d)?\\d*)" + ne + "*\\)|)(?=[^-]|$)", "i")
            }, he = /^(?:input|select|textarea|button)$/i, ge = /^h\d$/i, me = /^[^{]+\{\s*\[native \w/,
            ye = /^(?:#([\w-]+)|(\w+)|\.([\w-]+))$/, ve = /[+~]/, xe = /'|\\/g,
            be = new RegExp("\\\\([\\da-f]{1,6}" + ne + "?|(" + ne + ")|.)", "ig"), we = function (e, t, n) {
                var r = "0x" + t - 65536;
                return r !== r || n ? t : r < 0 ? String.fromCharCode(r + 65536) : String.fromCharCode(r >> 10 | 55296, 1023 & r | 56320)
            }, Te = function () {
                L()
            };
        try {
            Q.apply(J = Z.call(B.childNodes), B.childNodes), J[B.childNodes.length].nodeType
        } catch (Ce) {
            Q = {
                apply: J.length ? function (e, t) {
                    K.apply(e, Z.call(t))
                } : function (e, t) {
                    for (var n = e.length, r = 0; e[n++] = t[r++];) ;
                    e.length = n - 1
                }
            }
        }
        w = t.support = {}, E = t.isXML = function (e) {
            var t = e && (e.ownerDocument || e).documentElement;
            return !!t && "HTML" !== t.nodeName
        }, L = t.setDocument = function (e) {
            var t, n, r = e ? e.ownerDocument || e : B;
            return r !== H && 9 === r.nodeType && r.documentElement ? (H = r, q = H.documentElement, _ = !E(H), (n = H.defaultView) && n.top !== n && (n.addEventListener ? n.addEventListener("unload", Te, !1) : n.attachEvent && n.attachEvent("onunload", Te)), w.attributes = i(function (e) {
                return e.className = "i", !e.getAttribute("className")
            }), w.getElementsByTagName = i(function (e) {
                return e.appendChild(H.createComment("")), !e.getElementsByTagName("*").length
            }), w.getElementsByClassName = me.test(H.getElementsByClassName), w.getById = i(function (e) {
                return q.appendChild(e).id = P, !H.getElementsByName || !H.getElementsByName(P).length
            }), w.getById ? (T.find.ID = function (e, t) {
                if ("undefined" != typeof t.getElementById && _) {
                    var n = t.getElementById(e);
                    return n ? [n] : []
                }
            }, T.filter.ID = function (e) {
                var t = e.replace(be, we);
                return function (e) {
                    return e.getAttribute("id") === t
                }
            }) : (delete T.find.ID, T.filter.ID = function (e) {
                var t = e.replace(be, we);
                return function (e) {
                    var n = "undefined" != typeof e.getAttributeNode && e.getAttributeNode("id");
                    return n && n.value === t
                }
            }), T.find.TAG = w.getElementsByTagName ? function (e, t) {
                return "undefined" != typeof t.getElementsByTagName ? t.getElementsByTagName(e) : w.qsa ? t.querySelectorAll(e) : void 0
            } : function (e, t) {
                var n, r = [], i = 0, o = t.getElementsByTagName(e);
                if ("*" === e) {
                    for (; n = o[i++];) 1 === n.nodeType && r.push(n);
                    return r
                }
                return o
            }, T.find.CLASS = w.getElementsByClassName && function (e, t) {
                if ("undefined" != typeof t.getElementsByClassName && _) return t.getElementsByClassName(e)
            }, M = [], F = [], (w.qsa = me.test(H.querySelectorAll)) && (i(function (e) {
                q.appendChild(e).innerHTML = "<a id='" + P + "'></a><select id='" + P + "-\r\\' msallowcapture=''><option selected=''></option></select>", e.querySelectorAll("[msallowcapture^='']").length && F.push("[*^$]=" + ne + "*(?:''|\"\")"), e.querySelectorAll("[selected]").length || F.push("\\[" + ne + "*(?:value|" + te + ")"), e.querySelectorAll("[id~=" + P + "-]").length || F.push("~="), e.querySelectorAll(":checked").length || F.push(":checked"), e.querySelectorAll("a#" + P + "+*").length || F.push(".#.+[+~]")
            }), i(function (e) {
                var t = H.createElement("input");
                t.setAttribute("type", "hidden"), e.appendChild(t).setAttribute("name", "D"), e.querySelectorAll("[name=d]").length && F.push("name" + ne + "*[*^$|!~]?="), e.querySelectorAll(":enabled").length || F.push(":enabled", ":disabled"), e.querySelectorAll("*,:x"), F.push(",.*:")
            })), (w.matchesSelector = me.test(O = q.matches || q.webkitMatchesSelector || q.mozMatchesSelector || q.oMatchesSelector || q.msMatchesSelector)) && i(function (e) {
                w.disconnectedMatch = O.call(e, "div"), O.call(e, "[s!='']:x"), M.push("!=", oe)
            }), F = F.length && new RegExp(F.join("|")), M = M.length && new RegExp(M.join("|")), t = me.test(q.compareDocumentPosition), R = t || me.test(q.contains) ? function (e, t) {
                var n = 9 === e.nodeType ? e.documentElement : e, r = t && t.parentNode;
                return e === r || !(!r || 1 !== r.nodeType || !(n.contains ? n.contains(r) : e.compareDocumentPosition && 16 & e.compareDocumentPosition(r)))
            } : function (e, t) {
                if (t) for (; t = t.parentNode;) if (t === e) return !0;
                return !1
            }, U = t ? function (e, t) {
                if (e === t) return j = !0, 0;
                var n = !e.compareDocumentPosition - !t.compareDocumentPosition;
                return n ? n : (n = (e.ownerDocument || e) === (t.ownerDocument || t) ? e.compareDocumentPosition(t) : 1, 1 & n || !w.sortDetached && t.compareDocumentPosition(e) === n ? e === H || e.ownerDocument === B && R(B, e) ? -1 : t === H || t.ownerDocument === B && R(B, t) ? 1 : D ? ee(D, e) - ee(D, t) : 0 : 4 & n ? -1 : 1)
            } : function (e, t) {
                if (e === t) return j = !0, 0;
                var n, r = 0, i = e.parentNode, o = t.parentNode, s = [e], u = [t];
                if (!i || !o) return e === H ? -1 : t === H ? 1 : i ? -1 : o ? 1 : D ? ee(D, e) - ee(D, t) : 0;
                if (i === o) return a(e, t);
                for (n = e; n = n.parentNode;) s.unshift(n);
                for (n = t; n = n.parentNode;) u.unshift(n);
                for (; s[r] === u[r];) r++;
                return r ? a(s[r], u[r]) : s[r] === B ? -1 : u[r] === B ? 1 : 0
            }, H) : H
        }, t.matches = function (e, n) {
            return t(e, null, null, n)
        }, t.matchesSelector = function (e, n) {
            if ((e.ownerDocument || e) !== H && L(e), n = n.replace(ce, "='$1']"), w.matchesSelector && _ && !X[n + " "] && (!M || !M.test(n)) && (!F || !F.test(n))) try {
                var r = O.call(e, n);
                if (r || w.disconnectedMatch || e.document && 11 !== e.document.nodeType) return r
            } catch (i) {
            }
            return t(n, H, null, [e]).length > 0
        }, t.contains = function (e, t) {
            return (e.ownerDocument || e) !== H && L(e), R(e, t)
        }, t.attr = function (e, t) {
            (e.ownerDocument || e) !== H && L(e);
            var n = T.attrHandle[t.toLowerCase()],
                r = n && Y.call(T.attrHandle, t.toLowerCase()) ? n(e, t, !_) : void 0;
            return void 0 !== r ? r : w.attributes || !_ ? e.getAttribute(t) : (r = e.getAttributeNode(t)) && r.specified ? r.value : null
        }, t.error = function (e) {
            throw new Error("Syntax error, unrecognized expression: " + e)
        }, t.uniqueSort = function (e) {
            var t, n = [], r = 0, i = 0;
            if (j = !w.detectDuplicates, D = !w.sortStable && e.slice(0), e.sort(U), j) {
                for (; t = e[i++];) t === e[i] && (r = n.push(i));
                for (; r--;) e.splice(n[r], 1)
            }
            return D = null, e
        }, C = t.getText = function (e) {
            var t, n = "", r = 0, i = e.nodeType;
            if (i) {
                if (1 === i || 9 === i || 11 === i) {
                    if ("string" == typeof e.textContent) return e.textContent;
                    for (e = e.firstChild; e; e = e.nextSibling) n += C(e)
                } else if (3 === i || 4 === i) return e.nodeValue
            } else for (; t = e[r++];) n += C(t);
            return n
        }, T = t.selectors = {
            cacheLength: 50,
            createPseudo: r,
            match: pe,
            attrHandle: {},
            find: {},
            relative: {
                ">": {dir: "parentNode", first: !0},
                " ": {dir: "parentNode"},
                "+": {dir: "previousSibling", first: !0},
                "~": {dir: "previousSibling"}
            },
            preFilter: {
                ATTR: function (e) {
                    return e[1] = e[1].replace(be, we), e[3] = (e[3] || e[4] || e[5] || "").replace(be, we), "~=" === e[2] && (e[3] = " " + e[3] + " "), e.slice(0, 4)
                }, CHILD: function (e) {
                    return e[1] = e[1].toLowerCase(), "nth" === e[1].slice(0, 3) ? (e[3] || t.error(e[0]), e[4] = +(e[4] ? e[5] + (e[6] || 1) : 2 * ("even" === e[3] || "odd" === e[3])), e[5] = +(e[7] + e[8] || "odd" === e[3])) : e[3] && t.error(e[0]), e
                }, PSEUDO: function (e) {
                    var t, n = !e[6] && e[2];
                    return pe.CHILD.test(e[0]) ? null : (e[3] ? e[2] = e[4] || e[5] || "" : n && fe.test(n) && (t = N(n, !0)) && (t = n.indexOf(")", n.length - t) - n.length) && (e[0] = e[0].slice(0, t), e[2] = n.slice(0, t)), e.slice(0, 3))
                }
            },
            filter: {
                TAG: function (e) {
                    var t = e.replace(be, we).toLowerCase();
                    return "*" === e ? function () {
                        return !0
                    } : function (e) {
                        return e.nodeName && e.nodeName.toLowerCase() === t
                    }
                }, CLASS: function (e) {
                    var t = $[e + " "];
                    return t || (t = new RegExp("(^|" + ne + ")" + e + "(" + ne + "|$)")) && $(e, function (e) {
                        return t.test("string" == typeof e.className && e.className || "undefined" != typeof e.getAttribute && e.getAttribute("class") || "")
                    })
                }, ATTR: function (e, n, r) {
                    return function (i) {
                        var o = t.attr(i, e);
                        return null == o ? "!=" === n : !n || (o += "", "=" === n ? o === r : "!=" === n ? o !== r : "^=" === n ? r && 0 === o.indexOf(r) : "*=" === n ? r && o.indexOf(r) > -1 : "$=" === n ? r && o.slice(-r.length) === r : "~=" === n ? (" " + o.replace(ae, " ") + " ").indexOf(r) > -1 : "|=" === n && (o === r || o.slice(0, r.length + 1) === r + "-"))
                    }
                }, CHILD: function (e, t, n, r, i) {
                    var o = "nth" !== e.slice(0, 3), a = "last" !== e.slice(-4), s = "of-type" === t;
                    return 1 === r && 0 === i ? function (e) {
                        return !!e.parentNode
                    } : function (t, n, u) {
                        var l, c, f, d, p, h, g = o !== a ? "nextSibling" : "previousSibling", m = t.parentNode,
                            y = s && t.nodeName.toLowerCase(), v = !u && !s, x = !1;
                        if (m) {
                            if (o) {
                                for (; g;) {
                                    for (d = t; d = d[g];) if (s ? d.nodeName.toLowerCase() === y : 1 === d.nodeType) return !1;
                                    h = g = "only" === e && !h && "nextSibling"
                                }
                                return !0
                            }
                            if (h = [a ? m.firstChild : m.lastChild], a && v) {
                                for (d = m, f = d[P] || (d[P] = {}), c = f[d.uniqueID] || (f[d.uniqueID] = {}), l = c[e] || [], p = l[0] === W && l[1], x = p && l[2],
                                         d = p && m.childNodes[p]; d = ++p && d && d[g] || (x = p = 0) || h.pop();) if (1 === d.nodeType && ++x && d === t) {
                                    c[e] = [W, p, x];
                                    break
                                }
                            } else if (v && (d = t, f = d[P] || (d[P] = {}), c = f[d.uniqueID] || (f[d.uniqueID] = {}), l = c[e] || [], p = l[0] === W && l[1], x = p), x === !1) for (; (d = ++p && d && d[g] || (x = p = 0) || h.pop()) && ((s ? d.nodeName.toLowerCase() !== y : 1 !== d.nodeType) || !++x || (v && (f = d[P] || (d[P] = {}), c = f[d.uniqueID] || (f[d.uniqueID] = {}), c[e] = [W, x]), d !== t));) ;
                            return x -= i, x === r || x % r === 0 && x / r >= 0
                        }
                    }
                }, PSEUDO: function (e, n) {
                    var i, o = T.pseudos[e] || T.setFilters[e.toLowerCase()] || t.error("unsupported pseudo: " + e);
                    return o[P] ? o(n) : o.length > 1 ? (i = [e, e, "", n], T.setFilters.hasOwnProperty(e.toLowerCase()) ? r(function (e, t) {
                        for (var r, i = o(e, n), a = i.length; a--;) r = ee(e, i[a]), e[r] = !(t[r] = i[a])
                    }) : function (e) {
                        return o(e, 0, i)
                    }) : o
                }
            },
            pseudos: {
                not: r(function (e) {
                    var t = [], n = [], i = k(e.replace(se, "$1"));
                    return i[P] ? r(function (e, t, n, r) {
                        for (var o, a = i(e, null, r, []), s = e.length; s--;) (o = a[s]) && (e[s] = !(t[s] = o))
                    }) : function (e, r, o) {
                        return t[0] = e, i(t, null, o, n), t[0] = null, !n.pop()
                    }
                }), has: r(function (e) {
                    return function (n) {
                        return t(e, n).length > 0
                    }
                }), contains: r(function (e) {
                    return e = e.replace(be, we), function (t) {
                        return (t.textContent || t.innerText || C(t)).indexOf(e) > -1
                    }
                }), lang: r(function (e) {
                    return de.test(e || "") || t.error("unsupported lang: " + e), e = e.replace(be, we).toLowerCase(), function (t) {
                        var n;
                        do if (n = _ ? t.lang : t.getAttribute("xml:lang") || t.getAttribute("lang")) return n = n.toLowerCase(), n === e || 0 === n.indexOf(e + "-"); while ((t = t.parentNode) && 1 === t.nodeType);
                        return !1
                    }
                }), target: function (t) {
                    var n = e.location && e.location.hash;
                    return n && n.slice(1) === t.id
                }, root: function (e) {
                    return e === q
                }, focus: function (e) {
                    return e === H.activeElement && (!H.hasFocus || H.hasFocus()) && !!(e.type || e.href || ~e.tabIndex)
                }, enabled: function (e) {
                    return e.disabled === !1
                }, disabled: function (e) {
                    return e.disabled === !0
                }, checked: function (e) {
                    var t = e.nodeName.toLowerCase();
                    return "input" === t && !!e.checked || "option" === t && !!e.selected
                }, selected: function (e) {
                    return e.parentNode && e.parentNode.selectedIndex, e.selected === !0
                }, empty: function (e) {
                    for (e = e.firstChild; e; e = e.nextSibling) if (e.nodeType < 6) return !1;
                    return !0
                }, parent: function (e) {
                    return !T.pseudos.empty(e)
                }, header: function (e) {
                    return ge.test(e.nodeName)
                }, input: function (e) {
                    return he.test(e.nodeName)
                }, button: function (e) {
                    var t = e.nodeName.toLowerCase();
                    return "input" === t && "button" === e.type || "button" === t
                }, text: function (e) {
                    var t;
                    return "input" === e.nodeName.toLowerCase() && "text" === e.type && (null == (t = e.getAttribute("type")) || "text" === t.toLowerCase())
                }, first: l(function () {
                    return [0]
                }), last: l(function (e, t) {
                    return [t - 1]
                }), eq: l(function (e, t, n) {
                    return [n < 0 ? n + t : n]
                }), even: l(function (e, t) {
                    for (var n = 0; n < t; n += 2) e.push(n);
                    return e
                }), odd: l(function (e, t) {
                    for (var n = 1; n < t; n += 2) e.push(n);
                    return e
                }), lt: l(function (e, t, n) {
                    for (var r = n < 0 ? n + t : n; --r >= 0;) e.push(r);
                    return e
                }), gt: l(function (e, t, n) {
                    for (var r = n < 0 ? n + t : n; ++r < t;) e.push(r);
                    return e
                })
            }
        }, T.pseudos.nth = T.pseudos.eq;
        for (b in {radio: !0, checkbox: !0, file: !0, password: !0, image: !0}) T.pseudos[b] = s(b);
        for (b in {submit: !0, reset: !0}) T.pseudos[b] = u(b);
        return f.prototype = T.filters = T.pseudos, T.setFilters = new f, N = t.tokenize = function (e, n) {
            var r, i, o, a, s, u, l, c = z[e + " "];
            if (c) return n ? 0 : c.slice(0);
            for (s = e, u = [], l = T.preFilter; s;) {
                r && !(i = ue.exec(s)) || (i && (s = s.slice(i[0].length) || s), u.push(o = [])), r = !1, (i = le.exec(s)) && (r = i.shift(), o.push({
                    value: r,
                    type: i[0].replace(se, " ")
                }), s = s.slice(r.length));
                for (a in T.filter) !(i = pe[a].exec(s)) || l[a] && !(i = l[a](i)) || (r = i.shift(), o.push({
                    value: r,
                    type: a,
                    matches: i
                }), s = s.slice(r.length));
                if (!r) break
            }
            return n ? s.length : s ? t.error(e) : z(e, u).slice(0)
        }, k = t.compile = function (e, t) {
            var n, r = [], i = [], o = X[e + " "];
            if (!o) {
                for (t || (t = N(e)), n = t.length; n--;) o = v(t[n]), o[P] ? r.push(o) : i.push(o);
                o = X(e, x(i, r)), o.selector = e
            }
            return o
        }, S = t.select = function (e, t, n, r) {
            var i, o, a, s, u, l = "function" == typeof e && e, f = !r && N(e = l.selector || e);
            if (n = n || [], 1 === f.length) {
                if (o = f[0] = f[0].slice(0), o.length > 2 && "ID" === (a = o[0]).type && w.getById && 9 === t.nodeType && _ && T.relative[o[1].type]) {
                    if (t = (T.find.ID(a.matches[0].replace(be, we), t) || [])[0], !t) return n;
                    l && (t = t.parentNode), e = e.slice(o.shift().value.length)
                }
                for (i = pe.needsContext.test(e) ? 0 : o.length; i-- && (a = o[i], !T.relative[s = a.type]);) if ((u = T.find[s]) && (r = u(a.matches[0].replace(be, we), ve.test(o[0].type) && c(t.parentNode) || t))) {
                    if (o.splice(i, 1), e = r.length && d(o), !e) return Q.apply(n, r), n;
                    break
                }
            }
            return (l || k(e, f))(r, t, !_, n, !t || ve.test(e) && c(t.parentNode) || t), n
        }, w.sortStable = P.split("").sort(U).join("") === P, w.detectDuplicates = !!j, L(), w.sortDetached = i(function (e) {
            return 1 & e.compareDocumentPosition(H.createElement("div"))
        }), i(function (e) {
            return e.innerHTML = "<a href='#'></a>", "#" === e.firstChild.getAttribute("href")
        }) || o("type|href|height|width", function (e, t, n) {
            if (!n) return e.getAttribute(t, "type" === t.toLowerCase() ? 1 : 2)
        }), w.attributes && i(function (e) {
            return e.innerHTML = "<input/>", e.firstChild.setAttribute("value", ""), "" === e.firstChild.getAttribute("value")
        }) || o("value", function (e, t, n) {
            if (!n && "input" === e.nodeName.toLowerCase()) return e.defaultValue
        }), i(function (e) {
            return null == e.getAttribute("disabled")
        }) || o(te, function (e, t, n) {
            var r;
            if (!n) return e[t] === !0 ? t.toLowerCase() : (r = e.getAttributeNode(t)) && r.specified ? r.value : null
        }), t
    }(e);
    pe.find = ve, pe.expr = ve.selectors, pe.expr[":"] = pe.expr.pseudos, pe.uniqueSort = pe.unique = ve.uniqueSort, pe.text = ve.getText, pe.isXMLDoc = ve.isXML, pe.contains = ve.contains;
    var xe = function (e, t, n) {
        for (var r = [], i = void 0 !== n; (e = e[t]) && 9 !== e.nodeType;) if (1 === e.nodeType) {
            if (i && pe(e).is(n)) break;
            r.push(e)
        }
        return r
    }, be = function (e, t) {
        for (var n = []; e; e = e.nextSibling) 1 === e.nodeType && e !== t && n.push(e);
        return n
    }, we = pe.expr.match.needsContext, Te = /^<([\w-]+)\s*\/?>(?:<\/\1>|)$/, Ce = /^.[^:#\[\.,]*$/;
    pe.filter = function (e, t, n) {
        var r = t[0];
        return n && (e = ":not(" + e + ")"), 1 === t.length && 1 === r.nodeType ? pe.find.matchesSelector(r, e) ? [r] : [] : pe.find.matches(e, pe.grep(t, function (e) {
            return 1 === e.nodeType
        }))
    }, pe.fn.extend({
        find: function (e) {
            var t, n = [], r = this, i = r.length;
            if ("string" != typeof e) return this.pushStack(pe(e).filter(function () {
                for (t = 0; t < i; t++) if (pe.contains(r[t], this)) return !0
            }));
            for (t = 0; t < i; t++) pe.find(e, r[t], n);
            return n = this.pushStack(i > 1 ? pe.unique(n) : n), n.selector = this.selector ? this.selector + " " + e : e, n
        }, filter: function (e) {
            return this.pushStack(r(this, e || [], !1))
        }, not: function (e) {
            return this.pushStack(r(this, e || [], !0))
        }, is: function (e) {
            return !!r(this, "string" == typeof e && we.test(e) ? pe(e) : e || [], !1).length
        }
    });
    var Ee, Ne = /^(?:\s*(<[\w\W]+>)[^>]*|#([\w-]*))$/, ke = pe.fn.init = function (e, t, n) {
        var r, i;
        if (!e) return this;
        if (n = n || Ee, "string" == typeof e) {
            if (r = "<" === e.charAt(0) && ">" === e.charAt(e.length - 1) && e.length >= 3 ? [null, e, null] : Ne.exec(e), !r || !r[1] && t) return !t || t.jquery ? (t || n).find(e) : this.constructor(t).find(e);
            if (r[1]) {
                if (t = t instanceof pe ? t[0] : t, pe.merge(this, pe.parseHTML(r[1], t && t.nodeType ? t.ownerDocument || t : re, !0)), Te.test(r[1]) && pe.isPlainObject(t)) for (r in t) pe.isFunction(this[r]) ? this[r](t[r]) : this.attr(r, t[r]);
                return this
            }
            if (i = re.getElementById(r[2]), i && i.parentNode) {
                if (i.id !== r[2]) return Ee.find(e);
                this.length = 1, this[0] = i
            }
            return this.context = re, this.selector = e, this
        }
        return e.nodeType ? (this.context = this[0] = e, this.length = 1, this) : pe.isFunction(e) ? "undefined" != typeof n.ready ? n.ready(e) : e(pe) : (void 0 !== e.selector && (this.selector = e.selector, this.context = e.context), pe.makeArray(e, this))
    };
    ke.prototype = pe.fn, Ee = pe(re);
    var Se = /^(?:parents|prev(?:Until|All))/, Ae = {children: !0, contents: !0, next: !0, prev: !0};
    pe.fn.extend({
        has: function (e) {
            var t, n = pe(e, this), r = n.length;
            return this.filter(function () {
                for (t = 0; t < r; t++) if (pe.contains(this, n[t])) return !0
            })
        }, closest: function (e, t) {
            for (var n, r = 0, i = this.length, o = [], a = we.test(e) || "string" != typeof e ? pe(e, t || this.context) : 0; r < i; r++) for (n = this[r]; n && n !== t; n = n.parentNode) if (n.nodeType < 11 && (a ? a.index(n) > -1 : 1 === n.nodeType && pe.find.matchesSelector(n, e))) {
                o.push(n);
                break
            }
            return this.pushStack(o.length > 1 ? pe.uniqueSort(o) : o)
        }, index: function (e) {
            return e ? "string" == typeof e ? pe.inArray(this[0], pe(e)) : pe.inArray(e.jquery ? e[0] : e, this) : this[0] && this[0].parentNode ? this.first().prevAll().length : -1
        }, add: function (e, t) {
            return this.pushStack(pe.uniqueSort(pe.merge(this.get(), pe(e, t))))
        }, addBack: function (e) {
            return this.add(null == e ? this.prevObject : this.prevObject.filter(e))
        }
    }), pe.each({
        parent: function (e) {
            var t = e.parentNode;
            return t && 11 !== t.nodeType ? t : null
        }, parents: function (e) {
            return xe(e, "parentNode")
        }, parentsUntil: function (e, t, n) {
            return xe(e, "parentNode", n)
        }, next: function (e) {
            return i(e, "nextSibling")
        }, prev: function (e) {
            return i(e, "previousSibling")
        }, nextAll: function (e) {
            return xe(e, "nextSibling")
        }, prevAll: function (e) {
            return xe(e, "previousSibling")
        }, nextUntil: function (e, t, n) {
            return xe(e, "nextSibling", n)
        }, prevUntil: function (e, t, n) {
            return xe(e, "previousSibling", n)
        }, siblings: function (e) {
            return be((e.parentNode || {}).firstChild, e)
        }, children: function (e) {
            return be(e.firstChild)
        }, contents: function (e) {
            return pe.nodeName(e, "iframe") ? e.contentDocument || e.contentWindow.document : pe.merge([], e.childNodes)
        }
    }, function (e, t) {
        pe.fn[e] = function (n, r) {
            var i = pe.map(this, t, n);
            return "Until" !== e.slice(-5) && (r = n), r && "string" == typeof r && (i = pe.filter(r, i)), this.length > 1 && (Ae[e] || (i = pe.uniqueSort(i)), Se.test(e) && (i = i.reverse())), this.pushStack(i)
        }
    });
    var De = /\S+/g;
    pe.Callbacks = function (e) {
        e = "string" == typeof e ? o(e) : pe.extend({}, e);
        var t, n, r, i, a = [], s = [], u = -1, l = function () {
            for (i = e.once, r = t = !0; s.length; u = -1) for (n = s.shift(); ++u < a.length;) a[u].apply(n[0], n[1]) === !1 && e.stopOnFalse && (u = a.length, n = !1);
            e.memory || (n = !1), t = !1, i && (a = n ? [] : "")
        }, c = {
            add: function () {
                return a && (n && !t && (u = a.length - 1, s.push(n)), function r(t) {
                    pe.each(t, function (t, n) {
                        pe.isFunction(n) ? e.unique && c.has(n) || a.push(n) : n && n.length && "string" !== pe.type(n) && r(n)
                    })
                }(arguments), n && !t && l()), this
            }, remove: function () {
                return pe.each(arguments, function (e, t) {
                    for (var n; (n = pe.inArray(t, a, n)) > -1;) a.splice(n, 1), n <= u && u--
                }), this
            }, has: function (e) {
                return e ? pe.inArray(e, a) > -1 : a.length > 0
            }, empty: function () {
                return a && (a = []), this
            }, disable: function () {
                return i = s = [], a = n = "", this
            }, disabled: function () {
                return !a
            }, lock: function () {
                return i = !0, n || c.disable(), this
            }, locked: function () {
                return !!i
            }, fireWith: function (e, n) {
                return i || (n = n || [], n = [e, n.slice ? n.slice() : n], s.push(n), t || l()), this
            }, fire: function () {
                return c.fireWith(this, arguments), this
            }, fired: function () {
                return !!r
            }
        };
        return c
    }, pe.extend({
        Deferred: function (e) {
            var t = [["resolve", "done", pe.Callbacks("once memory"), "resolved"], ["reject", "fail", pe.Callbacks("once memory"), "rejected"], ["notify", "progress", pe.Callbacks("memory")]],
                n = "pending", r = {
                    state: function () {
                        return n
                    }, always: function () {
                        return i.done(arguments).fail(arguments), this
                    }, then: function () {
                        var e = arguments;
                        return pe.Deferred(function (n) {
                            pe.each(t, function (t, o) {
                                var a = pe.isFunction(e[t]) && e[t];
                                i[o[1]](function () {
                                    var e = a && a.apply(this, arguments);
                                    e && pe.isFunction(e.promise) ? e.promise().progress(n.notify).done(n.resolve).fail(n.reject) : n[o[0] + "With"](this === r ? n.promise() : this, a ? [e] : arguments)
                                })
                            }), e = null
                        }).promise()
                    }, promise: function (e) {
                        return null != e ? pe.extend(e, r) : r
                    }
                }, i = {};
            return r.pipe = r.then, pe.each(t, function (e, o) {
                var a = o[2], s = o[3];
                r[o[1]] = a.add, s && a.add(function () {
                    n = s
                }, t[1 ^ e][2].disable, t[2][2].lock), i[o[0]] = function () {
                    return i[o[0] + "With"](this === i ? r : this, arguments), this
                }, i[o[0] + "With"] = a.fireWith
            }), r.promise(i), e && e.call(i, i), i
        }, when: function (e) {
            var t, n, r, i = 0, o = ie.call(arguments), a = o.length,
                s = 1 !== a || e && pe.isFunction(e.promise) ? a : 0, u = 1 === s ? e : pe.Deferred(),
                l = function (e, n, r) {
                    return function (i) {
                        n[e] = this, r[e] = arguments.length > 1 ? ie.call(arguments) : i, r === t ? u.notifyWith(n, r) : --s || u.resolveWith(n, r)
                    }
                };
            if (a > 1) for (t = new Array(a), n = new Array(a), r = new Array(a); i < a; i++) o[i] && pe.isFunction(o[i].promise) ? o[i].promise().progress(l(i, n, t)).done(l(i, r, o)).fail(u.reject) : --s;
            return s || u.resolveWith(r, o), u.promise()
        }
    });
    var je;
    pe.fn.ready = function (e) {
        return pe.ready.promise().done(e), this
    }, pe.extend({
        isReady: !1, readyWait: 1, holdReady: function (e) {
            e ? pe.readyWait++ : pe.ready(!0)
        }, ready: function (e) {
            (e === !0 ? --pe.readyWait : pe.isReady) || (pe.isReady = !0, e !== !0 && --pe.readyWait > 0 || (je.resolveWith(re, [pe]), pe.fn.triggerHandler && (pe(re).triggerHandler("ready"), pe(re).off("ready"))))
        }
    }), pe.ready.promise = function (t) {
        if (!je) if (je = pe.Deferred(), "complete" === re.readyState || "loading" !== re.readyState && !re.documentElement.doScroll) e.setTimeout(pe.ready); else if (re.addEventListener) re.addEventListener("DOMContentLoaded", s), e.addEventListener("load", s); else {
            re.attachEvent("onreadystatechange", s), e.attachEvent("onload", s);
            var n = !1;
            try {
                n = null == e.frameElement && re.documentElement
            } catch (r) {
            }
            n && n.doScroll && !function i() {
                if (!pe.isReady) {
                    try {
                        n.doScroll("left")
                    } catch (t) {
                        return e.setTimeout(i, 50)
                    }
                    a(), pe.ready()
                }
            }()
        }
        return je.promise(t)
    }, pe.ready.promise();
    var Le;
    for (Le in pe(fe)) break;
    fe.ownFirst = "0" === Le, fe.inlineBlockNeedsLayout = !1, pe(function () {
        var e, t, n, r;
        n = re.getElementsByTagName("body")[0], n && n.style && (t = re.createElement("div"), r = re.createElement("div"), r.style.cssText = "position:absolute;border:0;width:0;height:0;top:0;left:-9999px", n.appendChild(r).appendChild(t), "undefined" != typeof t.style.zoom && (t.style.cssText = "display:inline;margin:0;border:0;padding:1px;width:1px;zoom:1", fe.inlineBlockNeedsLayout = e = 3 === t.offsetWidth, e && (n.style.zoom = 1)), n.removeChild(r))
    }), function () {
        var e = re.createElement("div");
        fe.deleteExpando = !0;
        try {
            delete e.test
        } catch (t) {
            fe.deleteExpando = !1
        }
        e = null
    }();
    var He = function (e) {
        var t = pe.noData[(e.nodeName + " ").toLowerCase()], n = +e.nodeType || 1;
        return (1 === n || 9 === n) && (!t || t !== !0 && e.getAttribute("classid") === t)
    }, qe = /^(?:\{[\w\W]*\}|\[[\w\W]*\])$/, _e = /([A-Z])/g;
    pe.extend({
        cache: {},
        noData: {"applet ": !0, "embed ": !0, "object ": "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"},
        hasData: function (e) {
            return e = e.nodeType ? pe.cache[e[pe.expando]] : e[pe.expando], !!e && !l(e)
        },
        data: function (e, t, n) {
            return c(e, t, n)
        },
        removeData: function (e, t) {
            return f(e, t)
        },
        _data: function (e, t, n) {
            return c(e, t, n, !0)
        },
        _removeData: function (e, t) {
            return f(e, t, !0)
        }
    }), pe.fn.extend({
        data: function (e, t) {
            var n, r, i, o = this[0], a = o && o.attributes;
            if (void 0 === e) {
                if (this.length && (i = pe.data(o), 1 === o.nodeType && !pe._data(o, "parsedAttrs"))) {
                    for (n = a.length; n--;) a[n] && (r = a[n].name, 0 === r.indexOf("data-") && (r = pe.camelCase(r.slice(5)), u(o, r, i[r])));
                    pe._data(o, "parsedAttrs", !0)
                }
                return i
            }
            return "object" == typeof e ? this.each(function () {
                pe.data(this, e)
            }) : arguments.length > 1 ? this.each(function () {
                pe.data(this, e, t)
            }) : o ? u(o, e, pe.data(o, e)) : void 0
        }, removeData: function (e) {
            return this.each(function () {
                pe.removeData(this, e)
            })
        }
    }), pe.extend({
        queue: function (e, t, n) {
            var r;
            if (e) return t = (t || "fx") + "queue", r = pe._data(e, t), n && (!r || pe.isArray(n) ? r = pe._data(e, t, pe.makeArray(n)) : r.push(n)), r || []
        }, dequeue: function (e, t) {
            t = t || "fx";
            var n = pe.queue(e, t), r = n.length, i = n.shift(), o = pe._queueHooks(e, t), a = function () {
                pe.dequeue(e, t)
            };
            "inprogress" === i && (i = n.shift(), r--), i && ("fx" === t && n.unshift("inprogress"), delete o.stop, i.call(e, a, o)), !r && o && o.empty.fire()
        }, _queueHooks: function (e, t) {
            var n = t + "queueHooks";
            return pe._data(e, n) || pe._data(e, n, {
                empty: pe.Callbacks("once memory").add(function () {
                    pe._removeData(e, t + "queue"), pe._removeData(e, n)
                })
            })
        }
    }), pe.fn.extend({
        queue: function (e, t) {
            var n = 2;
            return "string" != typeof e && (t = e, e = "fx", n--), arguments.length < n ? pe.queue(this[0], e) : void 0 === t ? this : this.each(function () {
                var n = pe.queue(this, e, t);
                pe._queueHooks(this, e), "fx" === e && "inprogress" !== n[0] && pe.dequeue(this, e)
            })
        }, dequeue: function (e) {
            return this.each(function () {
                pe.dequeue(this, e)
            })
        }, clearQueue: function (e) {
            return this.queue(e || "fx", [])
        }, promise: function (e, t) {
            var n, r = 1, i = pe.Deferred(), o = this, a = this.length, s = function () {
                --r || i.resolveWith(o, [o])
            };
            for ("string" != typeof e && (t = e, e = void 0), e = e || "fx"; a--;) n = pe._data(o[a], e + "queueHooks"), n && n.empty && (r++, n.empty.add(s));
            return s(), i.promise(t)
        }
    }), function () {
        var e;
        fe.shrinkWrapBlocks = function () {
            if (null != e) return e;
            e = !1;
            var t, n, r;
            return n = re.getElementsByTagName("body")[0], n && n.style ? (t = re.createElement("div"), r = re.createElement("div"), r.style.cssText = "position:absolute;border:0;width:0;height:0;top:0;left:-9999px", n.appendChild(r).appendChild(t), "undefined" != typeof t.style.zoom && (t.style.cssText = "-webkit-box-sizing:content-box;-moz-box-sizing:content-box;box-sizing:content-box;display:block;margin:0;border:0;padding:1px;width:1px;zoom:1", t.appendChild(re.createElement("div")).style.width = "5px", e = 3 !== t.offsetWidth), n.removeChild(r), e) : void 0
        }
    }();
    var Fe = /[+-]?(?:\d*\.|)\d+(?:[eE][+-]?\d+|)/.source, Me = new RegExp("^(?:([+-])=|)(" + Fe + ")([a-z%]*)$", "i"),
        Oe = ["Top", "Right", "Bottom", "Left"], Re = function (e, t) {
            return e = t || e, "none" === pe.css(e, "display") || !pe.contains(e.ownerDocument, e)
        }, Pe = function (e, t, n, r, i, o, a) {
            var s = 0, u = e.length, l = null == n;
            if ("object" === pe.type(n)) {
                i = !0;
                for (s in n) Pe(e, t, s, n[s], !0, o, a)
            } else if (void 0 !== r && (i = !0, pe.isFunction(r) || (a = !0), l && (a ? (t.call(e, r), t = null) : (l = t, t = function (e, t, n) {
                return l.call(pe(e), n)
            })), t)) for (; s < u; s++) t(e[s], n, a ? r : r.call(e[s], s, t(e[s], n)));
            return i ? e : l ? t.call(e) : u ? t(e[0], n) : o
        }, Be = /^(?:checkbox|radio)$/i, We = /<([\w:-]+)/, Ie = /^$|\/(?:java|ecma)script/i, $e = /^\s+/,
        ze = "abbr|article|aside|audio|bdi|canvas|data|datalist|details|dialog|figcaption|figure|footer|header|hgroup|main|mark|meter|nav|output|picture|progress|section|summary|template|time|video";
    !function () {
        var e = re.createElement("div"), t = re.createDocumentFragment(), n = re.createElement("input");
        e.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>", fe.leadingWhitespace = 3 === e.firstChild.nodeType, fe.tbody = !e.getElementsByTagName("tbody").length, fe.htmlSerialize = !!e.getElementsByTagName("link").length, fe.html5Clone = "<:nav></:nav>" !== re.createElement("nav").cloneNode(!0).outerHTML, n.type = "checkbox", n.checked = !0, t.appendChild(n), fe.appendChecked = n.checked, e.innerHTML = "<textarea>x</textarea>", fe.noCloneChecked = !!e.cloneNode(!0).lastChild.defaultValue, t.appendChild(e), n = re.createElement("input"), n.setAttribute("type", "radio"), n.setAttribute("checked", "checked"), n.setAttribute("name", "t"), e.appendChild(n), fe.checkClone = e.cloneNode(!0).cloneNode(!0).lastChild.checked, fe.noCloneEvent = !!e.addEventListener, e[pe.expando] = 1, fe.attributes = !e.getAttribute(pe.expando)
    }();
    var Xe = {
        option: [1, "<select multiple='multiple'>", "</select>"],
        legend: [1, "<fieldset>", "</fieldset>"],
        area: [1, "<map>", "</map>"],
        param: [1, "<object>", "</object>"],
        thead: [1, "<table>", "</table>"],
        tr: [2, "<table><tbody>", "</tbody></table>"],
        col: [2, "<table><tbody></tbody><colgroup>", "</colgroup></table>"],
        td: [3, "<table><tbody><tr>", "</tr></tbody></table>"],
        _default: fe.htmlSerialize ? [0, "", ""] : [1, "X<div>", "</div>"]
    };
    Xe.optgroup = Xe.option, Xe.tbody = Xe.tfoot = Xe.colgroup = Xe.caption = Xe.thead, Xe.th = Xe.td;
    var Ue = /<|&#?\w+;/, Ve = /<tbody/i;
    !function () {
        var t, n, r = re.createElement("div");
        for (t in {
            submit: !0,
            change: !0,
            focusin: !0
        }) n = "on" + t, (fe[t] = n in e) || (r.setAttribute(n, "t"), fe[t] = r.attributes[n].expando === !1);
        r = null
    }();
    var Ye = /^(?:input|select|textarea)$/i, Je = /^key/, Ge = /^(?:mouse|pointer|contextmenu|drag|drop)|click/,
        Ke = /^(?:focusinfocus|focusoutblur)$/, Qe = /^([^.]*)(?:\.(.+)|)/;
    pe.event = {
        global: {},
        add: function (e, t, n, r, i) {
            var o, a, s, u, l, c, f, d, p, h, g, m = pe._data(e);
            if (m) {
                for (n.handler && (u = n, n = u.handler, i = u.selector), n.guid || (n.guid = pe.guid++), (a = m.events) || (a = m.events = {}), (c = m.handle) || (c = m.handle = function (e) {
                    return "undefined" == typeof pe || e && pe.event.triggered === e.type ? void 0 : pe.event.dispatch.apply(c.elem, arguments)
                }, c.elem = e), t = (t || "").match(De) || [""], s = t.length; s--;) o = Qe.exec(t[s]) || [], p = g = o[1], h = (o[2] || "").split(".").sort(), p && (l = pe.event.special[p] || {}, p = (i ? l.delegateType : l.bindType) || p, l = pe.event.special[p] || {}, f = pe.extend({
                    type: p,
                    origType: g,
                    data: r,
                    handler: n,
                    guid: n.guid,
                    selector: i,
                    needsContext: i && pe.expr.match.needsContext.test(i),
                    namespace: h.join(".")
                }, u), (d = a[p]) || (d = a[p] = [], d.delegateCount = 0, l.setup && l.setup.call(e, r, h, c) !== !1 || (e.addEventListener ? e.addEventListener(p, c, !1) : e.attachEvent && e.attachEvent("on" + p, c))), l.add && (l.add.call(e, f), f.handler.guid || (f.handler.guid = n.guid)), i ? d.splice(d.delegateCount++, 0, f) : d.push(f), pe.event.global[p] = !0);
                e = null
            }
        },
        remove: function (e, t, n, r, i) {
            var o, a, s, u, l, c, f, d, p, h, g, m = pe.hasData(e) && pe._data(e);
            if (m && (c = m.events)) {
                for (t = (t || "").match(De) || [""], l = t.length; l--;) if (s = Qe.exec(t[l]) || [], p = g = s[1], h = (s[2] || "").split(".").sort(), p) {
                    for (f = pe.event.special[p] || {}, p = (r ? f.delegateType : f.bindType) || p, d = c[p] || [], s = s[2] && new RegExp("(^|\\.)" + h.join("\\.(?:.*\\.|)") + "(\\.|$)"), u = o = d.length; o--;) a = d[o], !i && g !== a.origType || n && n.guid !== a.guid || s && !s.test(a.namespace) || r && r !== a.selector && ("**" !== r || !a.selector) || (d.splice(o, 1), a.selector && d.delegateCount--, f.remove && f.remove.call(e, a));
                    u && !d.length && (f.teardown && f.teardown.call(e, h, m.handle) !== !1 || pe.removeEvent(e, p, m.handle), delete c[p])
                } else for (p in c) pe.event.remove(e, p + t[l], n, r, !0);
                pe.isEmptyObject(c) && (delete m.handle, pe._removeData(e, "events"))
            }
        },
        trigger: function (t, n, r, i) {
            var o, a, s, u, l, c, f, d = [r || re], p = ce.call(t, "type") ? t.type : t,
                h = ce.call(t, "namespace") ? t.namespace.split(".") : [];
            if (s = c = r = r || re, 3 !== r.nodeType && 8 !== r.nodeType && !Ke.test(p + pe.event.triggered) && (p.indexOf(".") > -1 && (h = p.split("."), p = h.shift(), h.sort()), a = p.indexOf(":") < 0 && "on" + p, t = t[pe.expando] ? t : new pe.Event(p, "object" == typeof t && t), t.isTrigger = i ? 2 : 3, t.namespace = h.join("."), t.rnamespace = t.namespace ? new RegExp("(^|\\.)" + h.join("\\.(?:.*\\.|)") + "(\\.|$)") : null, t.result = void 0, t.target || (t.target = r), n = null == n ? [t] : pe.makeArray(n, [t]), l = pe.event.special[p] || {}, i || !l.trigger || l.trigger.apply(r, n) !== !1)) {
                if (!i && !l.noBubble && !pe.isWindow(r)) {
                    for (u = l.delegateType || p, Ke.test(u + p) || (s = s.parentNode); s; s = s.parentNode) d.push(s), c = s;
                    c === (r.ownerDocument || re) && d.push(c.defaultView || c.parentWindow || e)
                }
                for (f = 0; (s = d[f++]) && !t.isPropagationStopped();) t.type = f > 1 ? u : l.bindType || p, o = (pe._data(s, "events") || {})[t.type] && pe._data(s, "handle"), o && o.apply(s, n), o = a && s[a], o && o.apply && He(s) && (t.result = o.apply(s, n), t.result === !1 && t.preventDefault());
                if (t.type = p, !i && !t.isDefaultPrevented() && (!l._default || l._default.apply(d.pop(), n) === !1) && He(r) && a && r[p] && !pe.isWindow(r)) {
                    c = r[a], c && (r[a] = null), pe.event.triggered = p;
                    try {
                        r[p]()
                    } catch (g) {
                    }
                    pe.event.triggered = void 0, c && (r[a] = c)
                }
                return t.result
            }
        },
        dispatch: function (e) {
            e = pe.event.fix(e);
            var t, n, r, i, o, a = [], s = ie.call(arguments), u = (pe._data(this, "events") || {})[e.type] || [],
                l = pe.event.special[e.type] || {};
            if (s[0] = e, e.delegateTarget = this, !l.preDispatch || l.preDispatch.call(this, e) !== !1) {
                for (a = pe.event.handlers.call(this, e, u), t = 0; (i = a[t++]) && !e.isPropagationStopped();) for (e.currentTarget = i.elem, n = 0; (o = i.handlers[n++]) && !e.isImmediatePropagationStopped();) e.rnamespace && !e.rnamespace.test(o.namespace) || (e.handleObj = o, e.data = o.data, r = ((pe.event.special[o.origType] || {}).handle || o.handler).apply(i.elem, s), void 0 !== r && (e.result = r) === !1 && (e.preventDefault(), e.stopPropagation()));
                return l.postDispatch && l.postDispatch.call(this, e), e.result
            }
        },
        handlers: function (e, t) {
            var n, r, i, o, a = [], s = t.delegateCount, u = e.target;
            if (s && u.nodeType && ("click" !== e.type || isNaN(e.button) || e.button < 1)) for (; u != this; u = u.parentNode || this) if (1 === u.nodeType && (u.disabled !== !0 || "click" !== e.type)) {
                for (r = [], n = 0; n < s; n++) o = t[n], i = o.selector + " ", void 0 === r[i] && (r[i] = o.needsContext ? pe(i, this).index(u) > -1 : pe.find(i, this, null, [u]).length), r[i] && r.push(o);
                r.length && a.push({elem: u, handlers: r})
            }
            return s < t.length && a.push({elem: this, handlers: t.slice(s)}), a
        },
        fix: function (e) {
            if (e[pe.expando]) return e;
            var t, n, r, i = e.type, o = e, a = this.fixHooks[i];
            for (a || (this.fixHooks[i] = a = Ge.test(i) ? this.mouseHooks : Je.test(i) ? this.keyHooks : {}), r = a.props ? this.props.concat(a.props) : this.props, e = new pe.Event(o), t = r.length; t--;) n = r[t], e[n] = o[n];
            return e.target || (e.target = o.srcElement || re), 3 === e.target.nodeType && (e.target = e.target.parentNode), e.metaKey = !!e.metaKey, a.filter ? a.filter(e, o) : e
        },
        props: "altKey bubbles cancelable ctrlKey currentTarget detail eventPhase metaKey relatedTarget shiftKey target timeStamp view which".split(" "),
        fixHooks: {},
        keyHooks: {
            props: "char charCode key keyCode".split(" "), filter: function (e, t) {
                return null == e.which && (e.which = null != t.charCode ? t.charCode : t.keyCode), e
            }
        },
        mouseHooks: {
            props: "button buttons clientX clientY fromElement offsetX offsetY pageX pageY screenX screenY toElement".split(" "),
            filter: function (e, t) {
                var n, r, i, o = t.button, a = t.fromElement;
                return null == e.pageX && null != t.clientX && (r = e.target.ownerDocument || re, i = r.documentElement, n = r.body, e.pageX = t.clientX + (i && i.scrollLeft || n && n.scrollLeft || 0) - (i && i.clientLeft || n && n.clientLeft || 0), e.pageY = t.clientY + (i && i.scrollTop || n && n.scrollTop || 0) - (i && i.clientTop || n && n.clientTop || 0)), !e.relatedTarget && a && (e.relatedTarget = a === e.target ? t.toElement : a), e.which || void 0 === o || (e.which = 1 & o ? 1 : 2 & o ? 3 : 4 & o ? 2 : 0), e
            }
        },
        special: {
            load: {noBubble: !0}, focus: {
                trigger: function () {
                    if (this !== b() && this.focus) try {
                        return this.focus(), !1
                    } catch (e) {
                    }
                }, delegateType: "focusin"
            }, blur: {
                trigger: function () {
                    if (this === b() && this.blur) return this.blur(), !1
                }, delegateType: "focusout"
            }, click: {
                trigger: function () {
                    if (pe.nodeName(this, "input") && "checkbox" === this.type && this.click) return this.click(), !1
                }, _default: function (e) {
                    return pe.nodeName(e.target, "a")
                }
            }, beforeunload: {
                postDispatch: function (e) {
                    void 0 !== e.result && e.originalEvent && (e.originalEvent.returnValue = e.result)
                }
            }
        },
        simulate: function (e, t, n) {
            var r = pe.extend(new pe.Event, n, {type: e, isSimulated: !0});
            pe.event.trigger(r, null, t), r.isDefaultPrevented() && n.preventDefault()
        }
    }, pe.removeEvent = re.removeEventListener ? function (e, t, n) {
        e.removeEventListener && e.removeEventListener(t, n)
    } : function (e, t, n) {
        var r = "on" + t;
        e.detachEvent && ("undefined" == typeof e[r] && (e[r] = null), e.detachEvent(r, n))
    }, pe.Event = function (e, t) {
        return this instanceof pe.Event ? (e && e.type ? (this.originalEvent = e, this.type = e.type, this.isDefaultPrevented = e.defaultPrevented || void 0 === e.defaultPrevented && e.returnValue === !1 ? v : x) : this.type = e, t && pe.extend(this, t), this.timeStamp = e && e.timeStamp || pe.now(), void (this[pe.expando] = !0)) : new pe.Event(e, t)
    }, pe.Event.prototype = {
        constructor: pe.Event,
        isDefaultPrevented: x,
        isPropagationStopped: x,
        isImmediatePropagationStopped: x,
        preventDefault: function () {
            var e = this.originalEvent;
            this.isDefaultPrevented = v, e && (e.preventDefault ? e.preventDefault() : e.returnValue = !1)
        },
        stopPropagation: function () {
            var e = this.originalEvent;
            this.isPropagationStopped = v, e && !this.isSimulated && (e.stopPropagation && e.stopPropagation(), e.cancelBubble = !0)
        },
        stopImmediatePropagation: function () {
            var e = this.originalEvent;
            this.isImmediatePropagationStopped = v, e && e.stopImmediatePropagation && e.stopImmediatePropagation(), this.stopPropagation()
        }
    }, pe.each({
        mouseenter: "mouseover",
        mouseleave: "mouseout",
        pointerenter: "pointerover",
        pointerleave: "pointerout"
    }, function (e, t) {
        pe.event.special[e] = {
            delegateType: t, bindType: t, handle: function (e) {
                var n, r = this, i = e.relatedTarget, o = e.handleObj;
                return i && (i === r || pe.contains(r, i)) || (e.type = o.origType, n = o.handler.apply(this, arguments), e.type = t), n
            }
        }
    }), fe.submit || (pe.event.special.submit = {
        setup: function () {
            return !pe.nodeName(this, "form") && void pe.event.add(this, "click._submit keypress._submit", function (e) {
                var t = e.target, n = pe.nodeName(t, "input") || pe.nodeName(t, "button") ? pe.prop(t, "form") : void 0;
                n && !pe._data(n, "submit") && (pe.event.add(n, "submit._submit", function (e) {
                    e._submitBubble = !0
                }), pe._data(n, "submit", !0))
            })
        }, postDispatch: function (e) {
            e._submitBubble && (delete e._submitBubble, this.parentNode && !e.isTrigger && pe.event.simulate("submit", this.parentNode, e))
        }, teardown: function () {
            return !pe.nodeName(this, "form") && void pe.event.remove(this, "._submit")
        }
    }), fe.change || (pe.event.special.change = {
        setup: function () {
            return Ye.test(this.nodeName) ? ("checkbox" !== this.type && "radio" !== this.type || (pe.event.add(this, "propertychange._change", function (e) {
                "checked" === e.originalEvent.propertyName && (this._justChanged = !0)
            }), pe.event.add(this, "click._change", function (e) {
                this._justChanged && !e.isTrigger && (this._justChanged = !1), pe.event.simulate("change", this, e)
            })), !1) : void pe.event.add(this, "beforeactivate._change", function (e) {
                var t = e.target;
                Ye.test(t.nodeName) && !pe._data(t, "change") && (pe.event.add(t, "change._change", function (e) {
                    !this.parentNode || e.isSimulated || e.isTrigger || pe.event.simulate("change", this.parentNode, e)
                }), pe._data(t, "change", !0))
            })
        }, handle: function (e) {
            var t = e.target;
            if (this !== t || e.isSimulated || e.isTrigger || "radio" !== t.type && "checkbox" !== t.type) return e.handleObj.handler.apply(this, arguments)
        }, teardown: function () {
            return pe.event.remove(this, "._change"), !Ye.test(this.nodeName)
        }
    }), fe.focusin || pe.each({focus: "focusin", blur: "focusout"}, function (e, t) {
        var n = function (e) {
            pe.event.simulate(t, e.target, pe.event.fix(e))
        };
        pe.event.special[t] = {
            setup: function () {
                var r = this.ownerDocument || this, i = pe._data(r, t);
                i || r.addEventListener(e, n, !0), pe._data(r, t, (i || 0) + 1)
            }, teardown: function () {
                var r = this.ownerDocument || this, i = pe._data(r, t) - 1;
                i ? pe._data(r, t, i) : (r.removeEventListener(e, n, !0), pe._removeData(r, t))
            }
        }
    }), pe.fn.extend({
        on: function (e, t, n, r) {
            return w(this, e, t, n, r)
        }, one: function (e, t, n, r) {
            return w(this, e, t, n, r, 1)
        }, off: function (e, t, n) {
            var r, i;
            if (e && e.preventDefault && e.handleObj) return r = e.handleObj, pe(e.delegateTarget).off(r.namespace ? r.origType + "." + r.namespace : r.origType, r.selector, r.handler), this;
            if ("object" == typeof e) {
                for (i in e) this.off(i, t, e[i]);
                return this
            }
            return t !== !1 && "function" != typeof t || (n = t, t = void 0), n === !1 && (n = x), this.each(function () {
                pe.event.remove(this, e, n, t)
            })
        }, trigger: function (e, t) {
            return this.each(function () {
                pe.event.trigger(e, t, this)
            })
        }, triggerHandler: function (e, t) {
            var n = this[0];
            if (n) return pe.event.trigger(e, t, n, !0)
        }
    });
    var Ze = / jQuery\d+="(?:null|\d+)"/g, et = new RegExp("<(?:" + ze + ")[\\s/>]", "i"),
        tt = /<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:-]+)[^>]*)\/>/gi, nt = /<script|<style|<link/i,
        rt = /checked\s*(?:[^=]|=\s*.checked.)/i, it = /^true\/(.*)/, ot = /^\s*<!(?:\[CDATA\[|--)|(?:\]\]|--)>\s*$/g,
        at = p(re), st = at.appendChild(re.createElement("div"));
    pe.extend({
        htmlPrefilter: function (e) {
            return e.replace(tt, "<$1></$2>")
        }, clone: function (e, t, n) {
            var r, i, o, a, s, u = pe.contains(e.ownerDocument, e);
            if (fe.html5Clone || pe.isXMLDoc(e) || !et.test("<" + e.nodeName + ">") ? o = e.cloneNode(!0) : (st.innerHTML = e.outerHTML, st.removeChild(o = st.firstChild)), !(fe.noCloneEvent && fe.noCloneChecked || 1 !== e.nodeType && 11 !== e.nodeType || pe.isXMLDoc(e))) for (r = h(o), s = h(e), a = 0; null != (i = s[a]); ++a) r[a] && k(i, r[a]);
            if (t) if (n) for (s = s || h(e), r = r || h(o), a = 0; null != (i = s[a]); a++) N(i, r[a]); else N(e, o);
            return r = h(o, "script"), r.length > 0 && g(r, !u && h(e, "script")), r = s = i = null, o
        }, cleanData: function (e, t) {
            for (var n, r, i, o, a = 0, s = pe.expando, u = pe.cache, l = fe.attributes, c = pe.event.special; null != (n = e[a]); a++) if ((t || He(n)) && (i = n[s], o = i && u[i])) {
                if (o.events) for (r in o.events) c[r] ? pe.event.remove(n, r) : pe.removeEvent(n, r, o.handle);
                u[i] && (delete u[i], l || "undefined" == typeof n.removeAttribute ? n[s] = void 0 : n.removeAttribute(s), ne.push(i))
            }
        }
    }), pe.fn.extend({
        domManip: S, detach: function (e) {
            return A(this, e, !0)
        }, remove: function (e) {
            return A(this, e)
        }, text: function (e) {
            return Pe(this, function (e) {
                return void 0 === e ? pe.text(this) : this.empty().append((this[0] && this[0].ownerDocument || re).createTextNode(e))
            }, null, e, arguments.length)
        }, append: function () {
            return S(this, arguments, function (e) {
                if (1 === this.nodeType || 11 === this.nodeType || 9 === this.nodeType) {
                    var t = T(this, e);
                    t.appendChild(e)
                }
            })
        }, prepend: function () {
            return S(this, arguments, function (e) {
                if (1 === this.nodeType || 11 === this.nodeType || 9 === this.nodeType) {
                    var t = T(this, e);
                    t.insertBefore(e, t.firstChild)
                }
            })
        }, before: function () {
            return S(this, arguments, function (e) {
                this.parentNode && this.parentNode.insertBefore(e, this)
            })
        }, after: function () {
            return S(this, arguments, function (e) {
                this.parentNode && this.parentNode.insertBefore(e, this.nextSibling)
            })
        }, empty: function () {
            for (var e, t = 0; null != (e = this[t]); t++) {
                for (1 === e.nodeType && pe.cleanData(h(e, !1)); e.firstChild;) e.removeChild(e.firstChild);
                e.options && pe.nodeName(e, "select") && (e.options.length = 0)
            }
            return this
        }, clone: function (e, t) {
            return e = null != e && e, t = null == t ? e : t, this.map(function () {
                return pe.clone(this, e, t)
            })
        }, html: function (e) {
            return Pe(this, function (e) {
                var t = this[0] || {}, n = 0, r = this.length;
                if (void 0 === e) return 1 === t.nodeType ? t.innerHTML.replace(Ze, "") : void 0;
                if ("string" == typeof e && !nt.test(e) && (fe.htmlSerialize || !et.test(e)) && (fe.leadingWhitespace || !$e.test(e)) && !Xe[(We.exec(e) || ["", ""])[1].toLowerCase()]) {
                    e = pe.htmlPrefilter(e);
                    try {
                        for (; n < r; n++) t = this[n] || {}, 1 === t.nodeType && (pe.cleanData(h(t, !1)), t.innerHTML = e);
                        t = 0
                    } catch (i) {
                    }
                }
                t && this.empty().append(e)
            }, null, e, arguments.length)
        }, replaceWith: function () {
            var e = [];
            return S(this, arguments, function (t) {
                var n = this.parentNode;
                pe.inArray(this, e) < 0 && (pe.cleanData(h(this)), n && n.replaceChild(t, this));
            }, e)
        }
    }), pe.each({
        appendTo: "append",
        prependTo: "prepend",
        insertBefore: "before",
        insertAfter: "after",
        replaceAll: "replaceWith"
    }, function (e, t) {
        pe.fn[e] = function (e) {
            for (var n, r = 0, i = [], o = pe(e), a = o.length - 1; r <= a; r++) n = r === a ? this : this.clone(!0), pe(o[r])[t](n), ae.apply(i, n.get());
            return this.pushStack(i)
        }
    });
    var ut, lt = {HTML: "block", BODY: "block"}, ct = /^margin/, ft = new RegExp("^(" + Fe + ")(?!px)[a-z%]+$", "i"),
        dt = function (e, t, n, r) {
            var i, o, a = {};
            for (o in t) a[o] = e.style[o], e.style[o] = t[o];
            i = n.apply(e, r || []);
            for (o in t) e.style[o] = a[o];
            return i
        }, pt = re.documentElement;
    !function () {
        function t() {
            var t, c, f = re.documentElement;
            f.appendChild(u), l.style.cssText = "-webkit-box-sizing:border-box;box-sizing:border-box;position:relative;display:block;margin:auto;border:1px;padding:1px;top:1%;width:50%", n = i = s = !1, r = a = !0, e.getComputedStyle && (c = e.getComputedStyle(l), n = "1%" !== (c || {}).top, s = "2px" === (c || {}).marginLeft, i = "4px" === (c || {width: "4px"}).width, l.style.marginRight = "50%", r = "4px" === (c || {marginRight: "4px"}).marginRight, t = l.appendChild(re.createElement("div")), t.style.cssText = l.style.cssText = "-webkit-box-sizing:content-box;-moz-box-sizing:content-box;box-sizing:content-box;display:block;margin:0;border:0;padding:0", t.style.marginRight = t.style.width = "0", l.style.width = "1px", a = !parseFloat((e.getComputedStyle(t) || {}).marginRight), l.removeChild(t)), l.style.display = "none", o = 0 === l.getClientRects().length, o && (l.style.display = "", l.innerHTML = "<table><tr><td></td><td>t</td></tr></table>", l.childNodes[0].style.borderCollapse = "separate", t = l.getElementsByTagName("td"), t[0].style.cssText = "margin:0;border:0;padding:0;display:none", o = 0 === t[0].offsetHeight, o && (t[0].style.display = "", t[1].style.display = "none", o = 0 === t[0].offsetHeight)), f.removeChild(u)
        }

        var n, r, i, o, a, s, u = re.createElement("div"), l = re.createElement("div");
        l.style && (l.style.cssText = "float:left;opacity:.5", fe.opacity = "0.5" === l.style.opacity, fe.cssFloat = !!l.style.cssFloat, l.style.backgroundClip = "content-box", l.cloneNode(!0).style.backgroundClip = "", fe.clearCloneStyle = "content-box" === l.style.backgroundClip, u = re.createElement("div"), u.style.cssText = "border:0;width:8px;height:0;top:0;left:-9999px;padding:0;margin-top:1px;position:absolute", l.innerHTML = "", u.appendChild(l), fe.boxSizing = "" === l.style.boxSizing || "" === l.style.MozBoxSizing || "" === l.style.WebkitBoxSizing, pe.extend(fe, {
            reliableHiddenOffsets: function () {
                return null == n && t(), o
            }, boxSizingReliable: function () {
                return null == n && t(), i
            }, pixelMarginRight: function () {
                return null == n && t(), r
            }, pixelPosition: function () {
                return null == n && t(), n
            }, reliableMarginRight: function () {
                return null == n && t(), a
            }, reliableMarginLeft: function () {
                return null == n && t(), s
            }
        }))
    }();
    var ht, gt, mt = /^(top|right|bottom|left)$/;
    e.getComputedStyle ? (ht = function (t) {
        var n = t.ownerDocument.defaultView;
        return n && n.opener || (n = e), n.getComputedStyle(t)
    }, gt = function (e, t, n) {
        var r, i, o, a, s = e.style;
        return n = n || ht(e), a = n ? n.getPropertyValue(t) || n[t] : void 0, "" !== a && void 0 !== a || pe.contains(e.ownerDocument, e) || (a = pe.style(e, t)), n && !fe.pixelMarginRight() && ft.test(a) && ct.test(t) && (r = s.width, i = s.minWidth, o = s.maxWidth, s.minWidth = s.maxWidth = s.width = a, a = n.width, s.width = r, s.minWidth = i, s.maxWidth = o), void 0 === a ? a : a + ""
    }) : pt.currentStyle && (ht = function (e) {
        return e.currentStyle
    }, gt = function (e, t, n) {
        var r, i, o, a, s = e.style;
        return n = n || ht(e), a = n ? n[t] : void 0, null == a && s && s[t] && (a = s[t]), ft.test(a) && !mt.test(t) && (r = s.left, i = e.runtimeStyle, o = i && i.left, o && (i.left = e.currentStyle.left), s.left = "fontSize" === t ? "1em" : a, a = s.pixelLeft + "px", s.left = r, o && (i.left = o)), void 0 === a ? a : a + "" || "auto"
    });
    var yt = /alpha\([^)]*\)/i, vt = /opacity\s*=\s*([^)]*)/i, xt = /^(none|table(?!-c[ea]).+)/,
        bt = new RegExp("^(" + Fe + ")(.*)$", "i"), wt = {position: "absolute", visibility: "hidden", display: "block"},
        Tt = {letterSpacing: "0", fontWeight: "400"}, Ct = ["Webkit", "O", "Moz", "ms"],
        Et = re.createElement("div").style;
    pe.extend({
        cssHooks: {
            opacity: {
                get: function (e, t) {
                    if (t) {
                        var n = gt(e, "opacity");
                        return "" === n ? "1" : n
                    }
                }
            }
        },
        cssNumber: {
            animationIterationCount: !0,
            columnCount: !0,
            fillOpacity: !0,
            flexGrow: !0,
            flexShrink: !0,
            fontWeight: !0,
            lineHeight: !0,
            opacity: !0,
            order: !0,
            orphans: !0,
            widows: !0,
            zIndex: !0,
            zoom: !0
        },
        cssProps: {"float": fe.cssFloat ? "cssFloat" : "styleFloat"},
        style: function (e, t, n, r) {
            if (e && 3 !== e.nodeType && 8 !== e.nodeType && e.style) {
                var i, o, a, s = pe.camelCase(t), u = e.style;
                if (t = pe.cssProps[s] || (pe.cssProps[s] = H(s) || s), a = pe.cssHooks[t] || pe.cssHooks[s], void 0 === n) return a && "get" in a && void 0 !== (i = a.get(e, !1, r)) ? i : u[t];
                if (o = typeof n, "string" === o && (i = Me.exec(n)) && i[1] && (n = d(e, t, i), o = "number"), null != n && n === n && ("number" === o && (n += i && i[3] || (pe.cssNumber[s] ? "" : "px")), fe.clearCloneStyle || "" !== n || 0 !== t.indexOf("background") || (u[t] = "inherit"), !(a && "set" in a && void 0 === (n = a.set(e, n, r))))) try {
                    u[t] = n
                } catch (l) {
                }
            }
        },
        css: function (e, t, n, r) {
            var i, o, a, s = pe.camelCase(t);
            return t = pe.cssProps[s] || (pe.cssProps[s] = H(s) || s), a = pe.cssHooks[t] || pe.cssHooks[s], a && "get" in a && (o = a.get(e, !0, n)), void 0 === o && (o = gt(e, t, r)), "normal" === o && t in Tt && (o = Tt[t]), "" === n || n ? (i = parseFloat(o), n === !0 || isFinite(i) ? i || 0 : o) : o
        }
    }), pe.each(["height", "width"], function (e, t) {
        pe.cssHooks[t] = {
            get: function (e, n, r) {
                if (n) return xt.test(pe.css(e, "display")) && 0 === e.offsetWidth ? dt(e, wt, function () {
                    return M(e, t, r)
                }) : M(e, t, r)
            }, set: function (e, n, r) {
                var i = r && ht(e);
                return _(e, n, r ? F(e, t, r, fe.boxSizing && "border-box" === pe.css(e, "boxSizing", !1, i), i) : 0)
            }
        }
    }), fe.opacity || (pe.cssHooks.opacity = {
        get: function (e, t) {
            return vt.test((t && e.currentStyle ? e.currentStyle.filter : e.style.filter) || "") ? .01 * parseFloat(RegExp.$1) + "" : t ? "1" : ""
        }, set: function (e, t) {
            var n = e.style, r = e.currentStyle, i = pe.isNumeric(t) ? "alpha(opacity=" + 100 * t + ")" : "",
                o = r && r.filter || n.filter || "";
            n.zoom = 1, (t >= 1 || "" === t) && "" === pe.trim(o.replace(yt, "")) && n.removeAttribute && (n.removeAttribute("filter"), "" === t || r && !r.filter) || (n.filter = yt.test(o) ? o.replace(yt, i) : o + " " + i)
        }
    }), pe.cssHooks.marginRight = L(fe.reliableMarginRight, function (e, t) {
        if (t) return dt(e, {display: "inline-block"}, gt, [e, "marginRight"])
    }), pe.cssHooks.marginLeft = L(fe.reliableMarginLeft, function (e, t) {
        if (t) return (parseFloat(gt(e, "marginLeft")) || (pe.contains(e.ownerDocument, e) ? e.getBoundingClientRect().left - dt(e, {marginLeft: 0}, function () {
            return e.getBoundingClientRect().left
        }) : 0)) + "px"
    }), pe.each({margin: "", padding: "", border: "Width"}, function (e, t) {
        pe.cssHooks[e + t] = {
            expand: function (n) {
                for (var r = 0, i = {}, o = "string" == typeof n ? n.split(" ") : [n]; r < 4; r++) i[e + Oe[r] + t] = o[r] || o[r - 2] || o[0];
                return i
            }
        }, ct.test(e) || (pe.cssHooks[e + t].set = _)
    }), pe.fn.extend({
        css: function (e, t) {
            return Pe(this, function (e, t, n) {
                var r, i, o = {}, a = 0;
                if (pe.isArray(t)) {
                    for (r = ht(e), i = t.length; a < i; a++) o[t[a]] = pe.css(e, t[a], !1, r);
                    return o
                }
                return void 0 !== n ? pe.style(e, t, n) : pe.css(e, t)
            }, e, t, arguments.length > 1)
        }, show: function () {
            return q(this, !0)
        }, hide: function () {
            return q(this)
        }, toggle: function (e) {
            return "boolean" == typeof e ? e ? this.show() : this.hide() : this.each(function () {
                Re(this) ? pe(this).show() : pe(this).hide()
            })
        }
    }), pe.Tween = O, O.prototype = {
        constructor: O, init: function (e, t, n, r, i, o) {
            this.elem = e, this.prop = n, this.easing = i || pe.easing._default, this.options = t, this.start = this.now = this.cur(), this.end = r, this.unit = o || (pe.cssNumber[n] ? "" : "px")
        }, cur: function () {
            var e = O.propHooks[this.prop];
            return e && e.get ? e.get(this) : O.propHooks._default.get(this)
        }, run: function (e) {
            var t, n = O.propHooks[this.prop];
            return this.options.duration ? this.pos = t = pe.easing[this.easing](e, this.options.duration * e, 0, 1, this.options.duration) : this.pos = t = e, this.now = (this.end - this.start) * t + this.start, this.options.step && this.options.step.call(this.elem, this.now, this), n && n.set ? n.set(this) : O.propHooks._default.set(this), this
        }
    }, O.prototype.init.prototype = O.prototype, O.propHooks = {
        _default: {
            get: function (e) {
                var t;
                return 1 !== e.elem.nodeType || null != e.elem[e.prop] && null == e.elem.style[e.prop] ? e.elem[e.prop] : (t = pe.css(e.elem, e.prop, ""), t && "auto" !== t ? t : 0)
            }, set: function (e) {
                pe.fx.step[e.prop] ? pe.fx.step[e.prop](e) : 1 !== e.elem.nodeType || null == e.elem.style[pe.cssProps[e.prop]] && !pe.cssHooks[e.prop] ? e.elem[e.prop] = e.now : pe.style(e.elem, e.prop, e.now + e.unit)
            }
        }
    }, O.propHooks.scrollTop = O.propHooks.scrollLeft = {
        set: function (e) {
            e.elem.nodeType && e.elem.parentNode && (e.elem[e.prop] = e.now)
        }
    }, pe.easing = {
        linear: function (e) {
            return e
        }, swing: function (e) {
            return .5 - Math.cos(e * Math.PI) / 2
        }, _default: "swing"
    }, pe.fx = O.prototype.init, pe.fx.step = {};
    var Nt, kt, St = /^(?:toggle|show|hide)$/, At = /queueHooks$/;
    pe.Animation = pe.extend($, {
        tweeners: {
            "*": [function (e, t) {
                var n = this.createTween(e, t);
                return d(n.elem, e, Me.exec(t), n), n
            }]
        }, tweener: function (e, t) {
            pe.isFunction(e) ? (t = e, e = ["*"]) : e = e.match(De);
            for (var n, r = 0, i = e.length; r < i; r++) n = e[r], $.tweeners[n] = $.tweeners[n] || [], $.tweeners[n].unshift(t)
        }, prefilters: [W], prefilter: function (e, t) {
            t ? $.prefilters.unshift(e) : $.prefilters.push(e)
        }
    }), pe.speed = function (e, t, n) {
        var r = e && "object" == typeof e ? pe.extend({}, e) : {
            complete: n || !n && t || pe.isFunction(e) && e,
            duration: e,
            easing: n && t || t && !pe.isFunction(t) && t
        };
        return r.duration = pe.fx.off ? 0 : "number" == typeof r.duration ? r.duration : r.duration in pe.fx.speeds ? pe.fx.speeds[r.duration] : pe.fx.speeds._default, null != r.queue && r.queue !== !0 || (r.queue = "fx"), r.old = r.complete, r.complete = function () {
            pe.isFunction(r.old) && r.old.call(this), r.queue && pe.dequeue(this, r.queue)
        }, r
    }, pe.fn.extend({
        fadeTo: function (e, t, n, r) {
            return this.filter(Re).css("opacity", 0).show().end().animate({opacity: t}, e, n, r)
        }, animate: function (e, t, n, r) {
            var i = pe.isEmptyObject(e), o = pe.speed(t, n, r), a = function () {
                var t = $(this, pe.extend({}, e), o);
                (i || pe._data(this, "finish")) && t.stop(!0)
            };
            return a.finish = a, i || o.queue === !1 ? this.each(a) : this.queue(o.queue, a)
        }, stop: function (e, t, n) {
            var r = function (e) {
                var t = e.stop;
                delete e.stop, t(n)
            };
            return "string" != typeof e && (n = t, t = e, e = void 0), t && e !== !1 && this.queue(e || "fx", []), this.each(function () {
                var t = !0, i = null != e && e + "queueHooks", o = pe.timers, a = pe._data(this);
                if (i) a[i] && a[i].stop && r(a[i]); else for (i in a) a[i] && a[i].stop && At.test(i) && r(a[i]);
                for (i = o.length; i--;) o[i].elem !== this || null != e && o[i].queue !== e || (o[i].anim.stop(n), t = !1, o.splice(i, 1));
                !t && n || pe.dequeue(this, e)
            })
        }, finish: function (e) {
            return e !== !1 && (e = e || "fx"), this.each(function () {
                var t, n = pe._data(this), r = n[e + "queue"], i = n[e + "queueHooks"], o = pe.timers,
                    a = r ? r.length : 0;
                for (n.finish = !0, pe.queue(this, e, []), i && i.stop && i.stop.call(this, !0), t = o.length; t--;) o[t].elem === this && o[t].queue === e && (o[t].anim.stop(!0), o.splice(t, 1));
                for (t = 0; t < a; t++) r[t] && r[t].finish && r[t].finish.call(this);
                delete n.finish
            })
        }
    }), pe.each(["toggle", "show", "hide"], function (e, t) {
        var n = pe.fn[t];
        pe.fn[t] = function (e, r, i) {
            return null == e || "boolean" == typeof e ? n.apply(this, arguments) : this.animate(P(t, !0), e, r, i)
        }
    }), pe.each({
        slideDown: P("show"),
        slideUp: P("hide"),
        slideToggle: P("toggle"),
        fadeIn: {opacity: "show"},
        fadeOut: {opacity: "hide"},
        fadeToggle: {opacity: "toggle"}
    }, function (e, t) {
        pe.fn[e] = function (e, n, r) {
            return this.animate(t, e, n, r)
        }
    }), pe.timers = [], pe.fx.tick = function () {
        var e, t = pe.timers, n = 0;
        for (Nt = pe.now(); n < t.length; n++) e = t[n], e() || t[n] !== e || t.splice(n--, 1);
        t.length || pe.fx.stop(), Nt = void 0
    }, pe.fx.timer = function (e) {
        pe.timers.push(e), e() ? pe.fx.start() : pe.timers.pop()
    }, pe.fx.interval = 13, pe.fx.start = function () {
        kt || (kt = e.setInterval(pe.fx.tick, pe.fx.interval))
    }, pe.fx.stop = function () {
        e.clearInterval(kt), kt = null
    }, pe.fx.speeds = {slow: 600, fast: 200, _default: 400}, pe.fn.delay = function (t, n) {
        return t = pe.fx ? pe.fx.speeds[t] || t : t, n = n || "fx", this.queue(n, function (n, r) {
            var i = e.setTimeout(n, t);
            r.stop = function () {
                e.clearTimeout(i)
            }
        })
    }, function () {
        var e, t = re.createElement("input"), n = re.createElement("div"), r = re.createElement("select"),
            i = r.appendChild(re.createElement("option"));
        n = re.createElement("div"), n.setAttribute("className", "t"), n.innerHTML = "  <link/><table></table><a href='/a'>a</a><input type='checkbox'/>", e = n.getElementsByTagName("a")[0], t.setAttribute("type", "checkbox"), n.appendChild(t), e = n.getElementsByTagName("a")[0], e.style.cssText = "top:1px", fe.getSetAttribute = "t" !== n.className, fe.style = /top/.test(e.getAttribute("style")), fe.hrefNormalized = "/a" === e.getAttribute("href"), fe.checkOn = !!t.value, fe.optSelected = i.selected, fe.enctype = !!re.createElement("form").enctype, r.disabled = !0, fe.optDisabled = !i.disabled, t = re.createElement("input"), t.setAttribute("value", ""), fe.input = "" === t.getAttribute("value"), t.value = "t", t.setAttribute("type", "radio"), fe.radioValue = "t" === t.value
    }();
    var Dt = /\r/g, jt = /[\x20\t\r\n\f]+/g;
    pe.fn.extend({
        val: function (e) {
            var t, n, r, i = this[0];
            {
                if (arguments.length) return r = pe.isFunction(e), this.each(function (n) {
                    var i;
                    1 === this.nodeType && (i = r ? e.call(this, n, pe(this).val()) : e, null == i ? i = "" : "number" == typeof i ? i += "" : pe.isArray(i) && (i = pe.map(i, function (e) {
                        return null == e ? "" : e + ""
                    })), t = pe.valHooks[this.type] || pe.valHooks[this.nodeName.toLowerCase()], t && "set" in t && void 0 !== t.set(this, i, "value") || (this.value = i))
                });
                if (i) return t = pe.valHooks[i.type] || pe.valHooks[i.nodeName.toLowerCase()], t && "get" in t && void 0 !== (n = t.get(i, "value")) ? n : (n = i.value, "string" == typeof n ? n.replace(Dt, "") : null == n ? "" : n)
            }
        }
    }), pe.extend({
        valHooks: {
            option: {
                get: function (e) {
                    var t = pe.find.attr(e, "value");
                    return null != t ? t : pe.trim(pe.text(e)).replace(jt, " ")
                }
            }, select: {
                get: function (e) {
                    for (var t, n, r = e.options, i = e.selectedIndex, o = "select-one" === e.type || i < 0, a = o ? null : [], s = o ? i + 1 : r.length, u = i < 0 ? s : o ? i : 0; u < s; u++) if (n = r[u], (n.selected || u === i) && (fe.optDisabled ? !n.disabled : null === n.getAttribute("disabled")) && (!n.parentNode.disabled || !pe.nodeName(n.parentNode, "optgroup"))) {
                        if (t = pe(n).val(), o) return t;
                        a.push(t)
                    }
                    return a
                }, set: function (e, t) {
                    for (var n, r, i = e.options, o = pe.makeArray(t), a = i.length; a--;) if (r = i[a], pe.inArray(pe.valHooks.option.get(r), o) > -1) try {
                        r.selected = n = !0
                    } catch (s) {
                        r.scrollHeight
                    } else r.selected = !1;
                    return n || (e.selectedIndex = -1), i
                }
            }
        }
    }), pe.each(["radio", "checkbox"], function () {
        pe.valHooks[this] = {
            set: function (e, t) {
                if (pe.isArray(t)) return e.checked = pe.inArray(pe(e).val(), t) > -1
            }
        }, fe.checkOn || (pe.valHooks[this].get = function (e) {
            return null === e.getAttribute("value") ? "on" : e.value
        })
    });
    var Lt, Ht, qt = pe.expr.attrHandle, _t = /^(?:checked|selected)$/i, Ft = fe.getSetAttribute, Mt = fe.input;
    pe.fn.extend({
        attr: function (e, t) {
            return Pe(this, pe.attr, e, t, arguments.length > 1)
        }, removeAttr: function (e) {
            return this.each(function () {
                pe.removeAttr(this, e)
            })
        }
    }), pe.extend({
        attr: function (e, t, n) {
            var r, i, o = e.nodeType;
            if (3 !== o && 8 !== o && 2 !== o) return "undefined" == typeof e.getAttribute ? pe.prop(e, t, n) : (1 === o && pe.isXMLDoc(e) || (t = t.toLowerCase(), i = pe.attrHooks[t] || (pe.expr.match.bool.test(t) ? Ht : Lt)), void 0 !== n ? null === n ? void pe.removeAttr(e, t) : i && "set" in i && void 0 !== (r = i.set(e, n, t)) ? r : (e.setAttribute(t, n + ""), n) : i && "get" in i && null !== (r = i.get(e, t)) ? r : (r = pe.find.attr(e, t), null == r ? void 0 : r))
        }, attrHooks: {
            type: {
                set: function (e, t) {
                    if (!fe.radioValue && "radio" === t && pe.nodeName(e, "input")) {
                        var n = e.value;
                        return e.setAttribute("type", t), n && (e.value = n), t
                    }
                }
            }
        }, removeAttr: function (e, t) {
            var n, r, i = 0, o = t && t.match(De);
            if (o && 1 === e.nodeType) for (; n = o[i++];) r = pe.propFix[n] || n, pe.expr.match.bool.test(n) ? Mt && Ft || !_t.test(n) ? e[r] = !1 : e[pe.camelCase("default-" + n)] = e[r] = !1 : pe.attr(e, n, ""), e.removeAttribute(Ft ? n : r)
        }
    }), Ht = {
        set: function (e, t, n) {
            return t === !1 ? pe.removeAttr(e, n) : Mt && Ft || !_t.test(n) ? e.setAttribute(!Ft && pe.propFix[n] || n, n) : e[pe.camelCase("default-" + n)] = e[n] = !0, n
        }
    }, pe.each(pe.expr.match.bool.source.match(/\w+/g), function (e, t) {
        var n = qt[t] || pe.find.attr;
        Mt && Ft || !_t.test(t) ? qt[t] = function (e, t, r) {
            var i, o;
            return r || (o = qt[t], qt[t] = i, i = null != n(e, t, r) ? t.toLowerCase() : null, qt[t] = o), i
        } : qt[t] = function (e, t, n) {
            if (!n) return e[pe.camelCase("default-" + t)] ? t.toLowerCase() : null
        }
    }), Mt && Ft || (pe.attrHooks.value = {
        set: function (e, t, n) {
            return pe.nodeName(e, "input") ? void (e.defaultValue = t) : Lt && Lt.set(e, t, n)
        }
    }), Ft || (Lt = {
        set: function (e, t, n) {
            var r = e.getAttributeNode(n);
            if (r || e.setAttributeNode(r = e.ownerDocument.createAttribute(n)), r.value = t += "", "value" === n || t === e.getAttribute(n)) return t
        }
    }, qt.id = qt.name = qt.coords = function (e, t, n) {
        var r;
        if (!n) return (r = e.getAttributeNode(t)) && "" !== r.value ? r.value : null
    }, pe.valHooks.button = {
        get: function (e, t) {
            var n = e.getAttributeNode(t);
            if (n && n.specified) return n.value
        }, set: Lt.set
    }, pe.attrHooks.contenteditable = {
        set: function (e, t, n) {
            Lt.set(e, "" !== t && t, n)
        }
    }, pe.each(["width", "height"], function (e, t) {
        pe.attrHooks[t] = {
            set: function (e, n) {
                if ("" === n) return e.setAttribute(t, "auto"), n
            }
        }
    })), fe.style || (pe.attrHooks.style = {
        get: function (e) {
            return e.style.cssText || void 0
        }, set: function (e, t) {
            return e.style.cssText = t + ""
        }
    });
    var Ot = /^(?:input|select|textarea|button|object)$/i, Rt = /^(?:a|area)$/i;
    pe.fn.extend({
        prop: function (e, t) {
            return Pe(this, pe.prop, e, t, arguments.length > 1)
        }, removeProp: function (e) {
            return e = pe.propFix[e] || e, this.each(function () {
                try {
                    this[e] = void 0, delete this[e]
                } catch (t) {
                }
            })
        }
    }), pe.extend({
        prop: function (e, t, n) {
            var r, i, o = e.nodeType;
            if (3 !== o && 8 !== o && 2 !== o) return 1 === o && pe.isXMLDoc(e) || (t = pe.propFix[t] || t, i = pe.propHooks[t]), void 0 !== n ? i && "set" in i && void 0 !== (r = i.set(e, n, t)) ? r : e[t] = n : i && "get" in i && null !== (r = i.get(e, t)) ? r : e[t]
        }, propHooks: {
            tabIndex: {
                get: function (e) {
                    var t = pe.find.attr(e, "tabindex");
                    return t ? parseInt(t, 10) : Ot.test(e.nodeName) || Rt.test(e.nodeName) && e.href ? 0 : -1
                }
            }
        }, propFix: {"for": "htmlFor", "class": "className"}
    }), fe.hrefNormalized || pe.each(["href", "src"], function (e, t) {
        pe.propHooks[t] = {
            get: function (e) {
                return e.getAttribute(t, 4)
            }
        }
    }), fe.optSelected || (pe.propHooks.selected = {
        get: function (e) {
            var t = e.parentNode;
            return t && (t.selectedIndex, t.parentNode && t.parentNode.selectedIndex), null
        }, set: function (e) {
            var t = e.parentNode;
            t && (t.selectedIndex, t.parentNode && t.parentNode.selectedIndex)
        }
    }), pe.each(["tabIndex", "readOnly", "maxLength", "cellSpacing", "cellPadding", "rowSpan", "colSpan", "useMap", "frameBorder", "contentEditable"], function () {
        pe.propFix[this.toLowerCase()] = this
    }), fe.enctype || (pe.propFix.enctype = "encoding");
    var Pt = /[\t\r\n\f]/g;
    pe.fn.extend({
        addClass: function (e) {
            var t, n, r, i, o, a, s, u = 0;
            if (pe.isFunction(e)) return this.each(function (t) {
                pe(this).addClass(e.call(this, t, z(this)))
            });
            if ("string" == typeof e && e) for (t = e.match(De) || []; n = this[u++];) if (i = z(n), r = 1 === n.nodeType && (" " + i + " ").replace(Pt, " ")) {
                for (a = 0; o = t[a++];) r.indexOf(" " + o + " ") < 0 && (r += o + " ");
                s = pe.trim(r), i !== s && pe.attr(n, "class", s)
            }
            return this
        }, removeClass: function (e) {
            var t, n, r, i, o, a, s, u = 0;
            if (pe.isFunction(e)) return this.each(function (t) {
                pe(this).removeClass(e.call(this, t, z(this)))
            });
            if (!arguments.length) return this.attr("class", "");
            if ("string" == typeof e && e) for (t = e.match(De) || []; n = this[u++];) if (i = z(n), r = 1 === n.nodeType && (" " + i + " ").replace(Pt, " ")) {
                for (a = 0; o = t[a++];) for (; r.indexOf(" " + o + " ") > -1;) r = r.replace(" " + o + " ", " ");
                s = pe.trim(r), i !== s && pe.attr(n, "class", s)
            }
            return this
        }, toggleClass: function (e, t) {
            var n = typeof e;
            return "boolean" == typeof t && "string" === n ? t ? this.addClass(e) : this.removeClass(e) : pe.isFunction(e) ? this.each(function (n) {
                pe(this).toggleClass(e.call(this, n, z(this), t), t)
            }) : this.each(function () {
                var t, r, i, o;
                if ("string" === n) for (r = 0, i = pe(this), o = e.match(De) || []; t = o[r++];) i.hasClass(t) ? i.removeClass(t) : i.addClass(t); else void 0 !== e && "boolean" !== n || (t = z(this), t && pe._data(this, "__className__", t), pe.attr(this, "class", t || e === !1 ? "" : pe._data(this, "__className__") || ""))
            })
        }, hasClass: function (e) {
            var t, n, r = 0;
            for (t = " " + e + " "; n = this[r++];) if (1 === n.nodeType && (" " + z(n) + " ").replace(Pt, " ").indexOf(t) > -1) return !0;
            return !1
        }
    }), pe.each("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error contextmenu".split(" "), function (e, t) {
        pe.fn[t] = function (e, n) {
            return arguments.length > 0 ? this.on(t, null, e, n) : this.trigger(t)
        }
    }), pe.fn.extend({
        hover: function (e, t) {
            return this.mouseenter(e).mouseleave(t || e)
        }
    });
    var Bt = e.location, Wt = pe.now(), It = /\?/,
        $t = /(,)|(\[|{)|(}|])|"(?:[^"\\\r\n]|\\["\\\/bfnrt]|\\u[\da-fA-F]{4})*"\s*:?|true|false|null|-?(?!0\d)\d+(?:\.\d+|)(?:[eE][+-]?\d+|)/g;
    pe.parseJSON = function (t) {
        if (e.JSON && e.JSON.parse) return e.JSON.parse(t + "");
        var n, r = null, i = pe.trim(t + "");
        return i && !pe.trim(i.replace($t, function (e, t, i, o) {
            return n && t && (r = 0), 0 === r ? e : (n = i || t, r += !o - !i, "")
        })) ? Function("return " + i)() : pe.error("Invalid JSON: " + t)
    }, pe.parseXML = function (t) {
        var n, r;
        if (!t || "string" != typeof t) return null;
        try {
            e.DOMParser ? (r = new e.DOMParser, n = r.parseFromString(t, "text/xml")) : (n = new e.ActiveXObject("Microsoft.XMLDOM"), n.async = "false", n.loadXML(t))
        } catch (i) {
            n = void 0
        }
        return n && n.documentElement && !n.getElementsByTagName("parsererror").length || pe.error("Invalid XML: " + t), n
    };
    var zt = /#.*$/, Xt = /([?&])_=[^&]*/, Ut = /^(.*?):[ \t]*([^\r\n]*)\r?$/gm,
        Vt = /^(?:about|app|app-storage|.+-extension|file|res|widget):$/, Yt = /^(?:GET|HEAD)$/, Jt = /^\/\//,
        Gt = /^([\w.+-]+:)(?:\/\/(?:[^\/?#]*@|)([^\/?#:]*)(?::(\d+)|)|)/, Kt = {}, Qt = {}, Zt = "*/".concat("*"),
        en = Bt.href, tn = Gt.exec(en.toLowerCase()) || [];
    pe.extend({
        active: 0,
        lastModified: {},
        etag: {},
        ajaxSettings: {
            url: en,
            type: "GET",
            isLocal: Vt.test(tn[1]),
            global: !0,
            processData: !0,
            async: !0,
            contentType: "application/x-www-form-urlencoded; charset=UTF-8",
            accepts: {
                "*": Zt,
                text: "text/plain",
                html: "text/html",
                xml: "application/xml, text/xml",
                json: "application/json, text/javascript"
            },
            contents: {xml: /\bxml\b/, html: /\bhtml/, json: /\bjson\b/},
            responseFields: {xml: "responseXML", text: "responseText", json: "responseJSON"},
            converters: {"* text": String, "text html": !0, "text json": pe.parseJSON, "text xml": pe.parseXML},
            flatOptions: {url: !0, context: !0}
        },
        ajaxSetup: function (e, t) {
            return t ? V(V(e, pe.ajaxSettings), t) : V(pe.ajaxSettings, e)
        },
        ajaxPrefilter: X(Kt),
        ajaxTransport: X(Qt),
        ajax: function (t, n) {
            function r(t, n, r, i) {
                var o, f, v, x, w, C = n;
                2 !== b && (b = 2, u && e.clearTimeout(u), c = void 0, s = i || "", T.readyState = t > 0 ? 4 : 0, o = t >= 200 && t < 300 || 304 === t, r && (x = Y(d, T, r)), x = J(d, x, T, o), o ? (d.ifModified && (w = T.getResponseHeader("Last-Modified"), w && (pe.lastModified[a] = w), w = T.getResponseHeader("etag"), w && (pe.etag[a] = w)), 204 === t || "HEAD" === d.type ? C = "nocontent" : 304 === t ? C = "notmodified" : (C = x.state, f = x.data, v = x.error, o = !v)) : (v = C, !t && C || (C = "error", t < 0 && (t = 0))), T.status = t, T.statusText = (n || C) + "", o ? g.resolveWith(p, [f, C, T]) : g.rejectWith(p, [T, C, v]), T.statusCode(y), y = void 0, l && h.trigger(o ? "ajaxSuccess" : "ajaxError", [T, d, o ? f : v]), m.fireWith(p, [T, C]), l && (h.trigger("ajaxComplete", [T, d]), --pe.active || pe.event.trigger("ajaxStop")))
            }

            "object" == typeof t && (n = t, t = void 0), n = n || {};
            var i, o, a, s, u, l, c, f, d = pe.ajaxSetup({}, n), p = d.context || d,
                h = d.context && (p.nodeType || p.jquery) ? pe(p) : pe.event, g = pe.Deferred(),
                m = pe.Callbacks("once memory"), y = d.statusCode || {}, v = {}, x = {}, b = 0, w = "canceled", T = {
                    readyState: 0, getResponseHeader: function (e) {
                        var t;
                        if (2 === b) {
                            if (!f) for (f = {}; t = Ut.exec(s);) f[t[1].toLowerCase()] = t[2];
                            t = f[e.toLowerCase()]
                        }
                        return null == t ? null : t
                    }, getAllResponseHeaders: function () {
                        return 2 === b ? s : null
                    }, setRequestHeader: function (e, t) {
                        var n = e.toLowerCase();
                        return b || (e = x[n] = x[n] || e, v[e] = t), this
                    }, overrideMimeType: function (e) {
                        return b || (d.mimeType = e), this
                    }, statusCode: function (e) {
                        var t;
                        if (e) if (b < 2) for (t in e) y[t] = [y[t], e[t]]; else T.always(e[T.status]);
                        return this
                    }, abort: function (e) {
                        var t = e || w;
                        return c && c.abort(t), r(0, t), this
                    }
                };
            if (g.promise(T).complete = m.add, T.success = T.done, T.error = T.fail, d.url = ((t || d.url || en) + "").replace(zt, "").replace(Jt, tn[1] + "//"), d.type = n.method || n.type || d.method || d.type, d.dataTypes = pe.trim(d.dataType || "*").toLowerCase().match(De) || [""], null == d.crossDomain && (i = Gt.exec(d.url.toLowerCase()), d.crossDomain = !(!i || i[1] === tn[1] && i[2] === tn[2] && (i[3] || ("http:" === i[1] ? "80" : "443")) === (tn[3] || ("http:" === tn[1] ? "80" : "443")))), d.data && d.processData && "string" != typeof d.data && (d.data = pe.param(d.data, d.traditional)), U(Kt, d, n, T), 2 === b) return T;
            l = pe.event && d.global, l && 0 === pe.active++ && pe.event.trigger("ajaxStart"), d.type = d.type.toUpperCase(), d.hasContent = !Yt.test(d.type), a = d.url, d.hasContent || (d.data && (a = d.url += (It.test(a) ? "&" : "?") + d.data, delete d.data), d.cache === !1 && (d.url = Xt.test(a) ? a.replace(Xt, "$1_=" + Wt++) : a + (It.test(a) ? "&" : "?") + "_=" + Wt++)), d.ifModified && (pe.lastModified[a] && T.setRequestHeader("If-Modified-Since", pe.lastModified[a]), pe.etag[a] && T.setRequestHeader("If-None-Match", pe.etag[a])), (d.data && d.hasContent && d.contentType !== !1 || n.contentType) && T.setRequestHeader("Content-Type", d.contentType), T.setRequestHeader("Accept", d.dataTypes[0] && d.accepts[d.dataTypes[0]] ? d.accepts[d.dataTypes[0]] + ("*" !== d.dataTypes[0] ? ", " + Zt + "; q=0.01" : "") : d.accepts["*"]);
            for (o in d.headers) T.setRequestHeader(o, d.headers[o]);
            if (d.beforeSend && (d.beforeSend.call(p, T, d) === !1 || 2 === b)) return T.abort();
            w = "abort";
            for (o in {success: 1, error: 1, complete: 1}) T[o](d[o]);
            if (c = U(Qt, d, n, T)) {
                if (T.readyState = 1, l && h.trigger("ajaxSend", [T, d]), 2 === b) return T;
                d.async && d.timeout > 0 && (u = e.setTimeout(function () {
                    T.abort("timeout")
                }, d.timeout));
                try {
                    b = 1, c.send(v, r)
                } catch (C) {
                    if (!(b < 2)) throw C;
                    r(-1, C)
                }
            } else r(-1, "No Transport");
            return T
        },
        getJSON: function (e, t, n) {
            return pe.get(e, t, n, "json")
        },
        getScript: function (e, t) {
            return pe.get(e, void 0, t, "script")
        }
    }), pe.each(["get", "post"], function (e, t) {
        pe[t] = function (e, n, r, i) {
            return pe.isFunction(n) && (i = i || r, r = n, n = void 0), pe.ajax(pe.extend({
                url: e,
                type: t,
                dataType: i,
                data: n,
                success: r
            }, pe.isPlainObject(e) && e))
        }
    }), pe._evalUrl = function (e) {
        return pe.ajax({url: e, type: "GET", dataType: "script", cache: !0, async: !1, global: !1, "throws": !0})
    }, pe.fn.extend({
        wrapAll: function (e) {
            if (pe.isFunction(e)) return this.each(function (t) {
                pe(this).wrapAll(e.call(this, t))
            });
            if (this[0]) {
                var t = pe(e, this[0].ownerDocument).eq(0).clone(!0);
                this[0].parentNode && t.insertBefore(this[0]), t.map(function () {
                    for (var e = this; e.firstChild && 1 === e.firstChild.nodeType;) e = e.firstChild;
                    return e
                }).append(this)
            }
            return this
        }, wrapInner: function (e) {
            return pe.isFunction(e) ? this.each(function (t) {
                pe(this).wrapInner(e.call(this, t))
            }) : this.each(function () {
                var t = pe(this), n = t.contents();
                n.length ? n.wrapAll(e) : t.append(e)
            })
        }, wrap: function (e) {
            var t = pe.isFunction(e);
            return this.each(function (n) {
                pe(this).wrapAll(t ? e.call(this, n) : e)
            })
        }, unwrap: function () {
            return this.parent().each(function () {
                pe.nodeName(this, "body") || pe(this).replaceWith(this.childNodes)
            }).end()
        }
    }), pe.expr.filters.hidden = function (e) {
        return fe.reliableHiddenOffsets() ? e.offsetWidth <= 0 && e.offsetHeight <= 0 && !e.getClientRects().length : K(e)
    }, pe.expr.filters.visible = function (e) {
        return !pe.expr.filters.hidden(e)
    };
    var nn = /%20/g, rn = /\[\]$/, on = /\r?\n/g, an = /^(?:submit|button|image|reset|file)$/i,
        sn = /^(?:input|select|textarea|keygen)/i;
    pe.param = function (e, t) {
        var n, r = [], i = function (e, t) {
            t = pe.isFunction(t) ? t() : null == t ? "" : t, r[r.length] = encodeURIComponent(e) + "=" + encodeURIComponent(t)
        };
        if (void 0 === t && (t = pe.ajaxSettings && pe.ajaxSettings.traditional), pe.isArray(e) || e.jquery && !pe.isPlainObject(e)) pe.each(e, function () {
            i(this.name, this.value)
        }); else for (n in e) Q(n, e[n], t, i);
        return r.join("&").replace(nn, "+")
    }, pe.fn.extend({
        serialize: function () {
            return pe.param(this.serializeArray())
        }, serializeArray: function () {
            return this.map(function () {
                var e = pe.prop(this, "elements");
                return e ? pe.makeArray(e) : this
            }).filter(function () {
                var e = this.type;
                return this.name && !pe(this).is(":disabled") && sn.test(this.nodeName) && !an.test(e) && (this.checked || !Be.test(e))
            }).map(function (e, t) {
                var n = pe(this).val();
                return null == n ? null : pe.isArray(n) ? pe.map(n, function (e) {
                    return {name: t.name, value: e.replace(on, "\r\n")}
                }) : {name: t.name, value: n.replace(on, "\r\n")}
            }).get()
        }
    }), pe.ajaxSettings.xhr = void 0 !== e.ActiveXObject ? function () {
        return this.isLocal ? ee() : re.documentMode > 8 ? Z() : /^(get|post|head|put|delete|options)$/i.test(this.type) && Z() || ee()
    } : Z;
    var un = 0, ln = {}, cn = pe.ajaxSettings.xhr();
    e.attachEvent && e.attachEvent("onunload", function () {
        for (var e in ln) ln[e](void 0, !0)
    }), fe.cors = !!cn && "withCredentials" in cn, cn = fe.ajax = !!cn, cn && pe.ajaxTransport(function (t) {
        if (!t.crossDomain || fe.cors) {
            var n;
            return {
                send: function (r, i) {
                    var o, a = t.xhr(), s = ++un;
                    if (a.open(t.type, t.url, t.async, t.username, t.password), t.xhrFields) for (o in t.xhrFields) a[o] = t.xhrFields[o];
                    t.mimeType && a.overrideMimeType && a.overrideMimeType(t.mimeType), t.crossDomain || r["X-Requested-With"] || (r["X-Requested-With"] = "XMLHttpRequest");
                    for (o in r) void 0 !== r[o] && a.setRequestHeader(o, r[o] + "");
                    a.send(t.hasContent && t.data || null), n = function (e, r) {
                        var o, u, l;
                        if (n && (r || 4 === a.readyState)) if (delete ln[s], n = void 0, a.onreadystatechange = pe.noop, r) 4 !== a.readyState && a.abort(); else {
                            l = {}, o = a.status, "string" == typeof a.responseText && (l.text = a.responseText);
                            try {
                                u = a.statusText
                            } catch (c) {
                                u = ""
                            }
                            o || !t.isLocal || t.crossDomain ? 1223 === o && (o = 204) : o = l.text ? 200 : 404
                        }
                        l && i(o, u, l, a.getAllResponseHeaders())
                    }, t.async ? 4 === a.readyState ? e.setTimeout(n) : a.onreadystatechange = ln[s] = n : n()
                }, abort: function () {
                    n && n(void 0, !0)
                }
            }
        }
    }), pe.ajaxSetup({
        accepts: {script: "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"},
        contents: {script: /\b(?:java|ecma)script\b/},
        converters: {
            "text script": function (e) {
                return pe.globalEval(e), e
            }
        }
    }), pe.ajaxPrefilter("script", function (e) {
        void 0 === e.cache && (e.cache = !1), e.crossDomain && (e.type = "GET", e.global = !1)
    }), pe.ajaxTransport("script", function (e) {
        if (e.crossDomain) {
            var t, n = re.head || pe("head")[0] || re.documentElement;
            return {
                send: function (r, i) {
                    t = re.createElement("script"), t.async = !0, e.scriptCharset && (t.charset = e.scriptCharset), t.src = e.url, t.onload = t.onreadystatechange = function (e, n) {
                        (n || !t.readyState || /loaded|complete/.test(t.readyState)) && (t.onload = t.onreadystatechange = null, t.parentNode && t.parentNode.removeChild(t), t = null, n || i(200, "success"))
                    }, n.insertBefore(t, n.firstChild)
                }, abort: function () {
                    t && t.onload(void 0, !0)
                }
            }
        }
    });
    var fn = [], dn = /(=)\?(?=&|$)|\?\?/;
    pe.ajaxSetup({
        jsonp: "callback", jsonpCallback: function () {
            var e = fn.pop() || pe.expando + "_" + Wt++;
            return this[e] = !0, e
        }
    }), pe.ajaxPrefilter("json jsonp", function (t, n, r) {
        var i, o, a,
            s = t.jsonp !== !1 && (dn.test(t.url) ? "url" : "string" == typeof t.data && 0 === (t.contentType || "").indexOf("application/x-www-form-urlencoded") && dn.test(t.data) && "data");
        if (s || "jsonp" === t.dataTypes[0]) return i = t.jsonpCallback = pe.isFunction(t.jsonpCallback) ? t.jsonpCallback() : t.jsonpCallback, s ? t[s] = t[s].replace(dn, "$1" + i) : t.jsonp !== !1 && (t.url += (It.test(t.url) ? "&" : "?") + t.jsonp + "=" + i), t.converters["script json"] = function () {
            return a || pe.error(i + " was not called"), a[0]
        }, t.dataTypes[0] = "json", o = e[i], e[i] = function () {
            a = arguments
        }, r.always(function () {
            void 0 === o ? pe(e).removeProp(i) : e[i] = o, t[i] && (t.jsonpCallback = n.jsonpCallback, fn.push(i)), a && pe.isFunction(o) && o(a[0]), a = o = void 0
        }), "script"
    }), pe.parseHTML = function (e, t, n) {
        if (!e || "string" != typeof e) return null;
        "boolean" == typeof t && (n = t, t = !1), t = t || re;
        var r = Te.exec(e), i = !n && [];
        return r ? [t.createElement(r[1])] : (r = y([e], t, i), i && i.length && pe(i).remove(), pe.merge([], r.childNodes))
    };
    var pn = pe.fn.load;
    return pe.fn.load = function (e, t, n) {
        if ("string" != typeof e && pn) return pn.apply(this, arguments);
        var r, i, o, a = this, s = e.indexOf(" ");
        return s > -1 && (r = pe.trim(e.slice(s, e.length)), e = e.slice(0, s)), pe.isFunction(t) ? (n = t, t = void 0) : t && "object" == typeof t && (i = "POST"), a.length > 0 && pe.ajax({
            url: e,
            type: i || "GET",
            dataType: "html",
            data: t
        }).done(function (e) {
            o = arguments, a.html(r ? pe("<div>").append(pe.parseHTML(e)).find(r) : e)
        }).always(n && function (e, t) {
            a.each(function () {
                n.apply(this, o || [e.responseText, t, e])
            })
        }), this
    }, pe.each(["ajaxStart", "ajaxStop", "ajaxComplete", "ajaxError", "ajaxSuccess", "ajaxSend"], function (e, t) {
        pe.fn[t] = function (e) {
            return this.on(t, e)
        }
    }), pe.expr.filters.animated = function (e) {
        return pe.grep(pe.timers, function (t) {
            return e === t.elem
        }).length
    }, pe.offset = {
        setOffset: function (e, t, n) {
            var r, i, o, a, s, u, l, c = pe.css(e, "position"), f = pe(e), d = {};
            "static" === c && (e.style.position = "relative"), s = f.offset(), o = pe.css(e, "top"), u = pe.css(e, "left"), l = ("absolute" === c || "fixed" === c) && pe.inArray("auto", [o, u]) > -1, l ? (r = f.position(), a = r.top, i = r.left) : (a = parseFloat(o) || 0, i = parseFloat(u) || 0), pe.isFunction(t) && (t = t.call(e, n, pe.extend({}, s))), null != t.top && (d.top = t.top - s.top + a), null != t.left && (d.left = t.left - s.left + i), "using" in t ? t.using.call(e, d) : f.css(d)
        }
    }, pe.fn.extend({
        offset: function (e) {
            if (arguments.length) return void 0 === e ? this : this.each(function (t) {
                pe.offset.setOffset(this, e, t)
            });
            var t, n, r = {top: 0, left: 0}, i = this[0], o = i && i.ownerDocument;
            if (o) return t = o.documentElement, pe.contains(t, i) ? ("undefined" != typeof i.getBoundingClientRect && (r = i.getBoundingClientRect()), n = te(o), {
                top: r.top + (n.pageYOffset || t.scrollTop) - (t.clientTop || 0),
                left: r.left + (n.pageXOffset || t.scrollLeft) - (t.clientLeft || 0)
            }) : r
        }, position: function () {
            if (this[0]) {
                var e, t, n = {top: 0, left: 0}, r = this[0];
                return "fixed" === pe.css(r, "position") ? t = r.getBoundingClientRect() : (e = this.offsetParent(), t = this.offset(), pe.nodeName(e[0], "html") || (n = e.offset()), n.top += pe.css(e[0], "borderTopWidth", !0), n.left += pe.css(e[0], "borderLeftWidth", !0)), {
                    top: t.top - n.top - pe.css(r, "marginTop", !0),
                    left: t.left - n.left - pe.css(r, "marginLeft", !0)
                }
            }
        }, offsetParent: function () {
            return this.map(function () {
                for (var e = this.offsetParent; e && !pe.nodeName(e, "html") && "static" === pe.css(e, "position");) e = e.offsetParent;
                return e || pt
            })
        }
    }), pe.each({scrollLeft: "pageXOffset", scrollTop: "pageYOffset"}, function (e, t) {
        var n = /Y/.test(t);
        pe.fn[e] = function (r) {
            return Pe(this, function (e, r, i) {
                var o = te(e);
                return void 0 === i ? o ? t in o ? o[t] : o.document.documentElement[r] : e[r] : void (o ? o.scrollTo(n ? pe(o).scrollLeft() : i, n ? i : pe(o).scrollTop()) : e[r] = i)
            }, e, r, arguments.length, null)
        }
    }), pe.each(["top", "left"], function (e, t) {
        pe.cssHooks[t] = L(fe.pixelPosition, function (e, n) {
            if (n) return n = gt(e, t), ft.test(n) ? pe(e).position()[t] + "px" : n
        })
    }), pe.each({Height: "height", Width: "width"}, function (e, t) {
        pe.each({padding: "inner" + e, content: t, "": "outer" + e}, function (n, r) {
            pe.fn[r] = function (r, i) {
                var o = arguments.length && (n || "boolean" != typeof r),
                    a = n || (r === !0 || i === !0 ? "margin" : "border");
                return Pe(this, function (t, n, r) {
                    var i;
                    return pe.isWindow(t) ? t.document.documentElement["client" + e] : 9 === t.nodeType ? (i = t.documentElement, Math.max(t.body["scroll" + e], i["scroll" + e], t.body["offset" + e], i["offset" + e], i["client" + e])) : void 0 === r ? pe.css(t, n, a) : pe.style(t, n, r, a)
                }, t, o ? r : void 0, o, null)
            }
        })
    }), pe.fn.extend({
        bind: function (e, t, n) {
            return this.on(e, null, t, n)
        }, unbind: function (e, t) {
            return this.off(e, null, t)
        }, delegate: function (e, t, n, r) {
            return this.on(t, e, n, r)
        }, undelegate: function (e, t, n) {
            return 1 === arguments.length ? this.off(e, "**") : this.off(t, e || "**", n)
        }
    }), pe.fn.size = function () {
        return this.length
    }, pe.fn.andSelf = pe.fn.addBack, layui.define(function (e) {
        layui.$ = pe, e("jquery", pe)
    }), pe
});
!function (e, t) {
    "use strict";
    var i, n, a = e.layui && layui.define, o = {
        getPath: function () {
            var e = document.currentScript ? document.currentScript.src : function () {
                for (var e, t = document.scripts, i = t.length - 1, n = i; n > 0; n--) if ("interactive" === t[n].readyState) {
                    e = t[n].src;
                    break
                }
                return e || t[i].src
            }();
            return e.substring(0, e.lastIndexOf("/") + 1)
        }(),
        config: {},
        end: {},
        minIndex: 0,
        minLeft: [],
        btn: ["&#x786E;&#x5B9A;", "&#x53D6;&#x6D88;"],
        type: ["dialog", "page", "iframe", "loading", "tips"],
        getStyle: function (t, i) {
            var n = t.currentStyle ? t.currentStyle : e.getComputedStyle(t, null);
            return n[n.getPropertyValue ? "getPropertyValue" : "getAttribute"](i)
        },
        link: function (t, i, n) {
            if (r.path) {
                var a = document.getElementsByTagName("head")[0], s = document.createElement("link");
                "string" == typeof i && (n = i);
                var l = (n || t).replace(/\.|\//g, ""), f = "layuicss-" + l, c = "creating", u = 0;
                s.rel = "stylesheet", s.href = r.path + t, s.id = f, document.getElementById(f) || a.appendChild(s), "function" == typeof i && !function d(t) {
                    var n = 100, a = document.getElementById(f);
                    return ++u > 1e4 / n ? e.console && console.error(l + ".css: Invalid") : void (1989 === parseInt(o.getStyle(a, "width")) ? (t === c && a.removeAttribute("lay-status"), a.getAttribute("lay-status") === c ? setTimeout(d, n) : i()) : (a.setAttribute("lay-status", c), setTimeout(function () {
                        d(c)
                    }, n)))
                }()
            }
        }
    }, r = {
        v: "3.4.0", ie: function () {
            var t = navigator.userAgent.toLowerCase();
            return !!(e.ActiveXObject || "ActiveXObject" in e) && ((t.match(/msie\s(\d+)/) || [])[1] || "11")
        }(), index: e.layer && e.layer.v ? 1e5 : 0, path: o.getPath, config: function (e, t) {
            return e = e || {}, r.cache = o.config = i.extend({}, o.config, e), r.path = o.config.path || r.path, "string" == typeof e.extend && (e.extend = [e.extend]), o.config.path && r.ready(), e.extend ? (a ? layui.addcss("modules/layer/" + e.extend) : o.link("theme/" + e.extend), this) : this
        }, ready: function (e) {
            var t = "layer", i = "", n = (a ? "modules/layer/" : "theme/") + "default/layer.css?v=" + r.v + i;
            // return a ? layui.addcss(n, e, t) : o.link(n, e, t), this
            return null, this
        }, alert: function (e, t, n) {
            var a = "function" == typeof t;
            return a && (n = t), r.open(i.extend({content: e, yes: n}, a ? {} : t))
        }, confirm: function (e, t, n, a) {
            var s = "function" == typeof t;
            return s && (a = n, n = t), r.open(i.extend({content: e, btn: o.btn, yes: n, btn2: a}, s ? {} : t))
        }, msg: function (e, n, a) {
            var s = "function" == typeof n, f = o.config.skin, c = (f ? f + " " + f + "-msg" : "") || "layui-layer-msg",
                u = l.anim.length - 1;
            return s && (a = n), r.open(i.extend({
                content: e,
                time: 3e3,
                shade: !1,
                skin: c,
                title: !1,
                closeBtn: !1,
                btn: !1,
                resize: !1,
                end: a
            }, s && !o.config.skin ? {skin: c + " layui-layer-hui", anim: u} : function () {
                return n = n || {}, (n.icon === -1 || n.icon === t && !o.config.skin) && (n.skin = c + " " + (n.skin || "layui-layer-hui")), n
            }()))
        }, load: function (e, t) {
            return r.open(i.extend({type: 3, icon: e || 0, resize: !1, shade: .01}, t))
        }, tips: function (e, t, n) {
            return r.open(i.extend({
                type: 4,
                content: [e, t],
                closeBtn: !1,
                time: 3e3,
                shade: !1,
                resize: !1,
                fixed: !1,
                maxWidth: 260
            }, n))
        }
    }, s = function (e) {
        var t = this, a = function () {
            r.ready(function () {
                t.creat()
            })
        };
        t.index = ++r.index, t.config.maxWidth = i(n).width() - 30, t.config = i.extend({}, t.config, o.config, e), document.body ? a() : setTimeout(function () {
            a()
        }, 30)
    };
    s.pt = s.prototype;
    var l = ["layui-layer", ".layui-layer-title", ".layui-layer-main", ".layui-layer-dialog", "layui-layer-iframe", "layui-layer-content", "layui-layer-btn", "layui-layer-close"];
    l.anim = ["layer-anim-00", "layer-anim-01", "layer-anim-02", "layer-anim-03", "layer-anim-04", "layer-anim-05", "layer-anim-06"], s.pt.config = {
        type: 0,
        shade: .3,
        fixed: !0,
        move: l[1],
        title: "&#x4FE1;&#x606F;",
        offset: "auto",
        area: "auto",
        closeBtn: 1,
        time: 0,
        zIndex: 19891014,
        maxWidth: 360,
        anim: 0,
        isOutAnim: !0,
        icon: -1,
        moveType: 1,
        resize: !0,
        scrollbar: !0,
        tips: 2
    }, s.pt.vessel = function (e, t) {
        var n = this, a = n.index, r = n.config, s = r.zIndex + a, f = "object" == typeof r.title,
            c = r.maxmin && (1 === r.type || 2 === r.type),
            u = r.title ? '<div class="layui-layer-title" style="' + (f ? r.title[1] : "") + '">' + (f ? r.title[0] : r.title) + "</div>" : "";
        return r.zIndex = s, t([r.shade ? '<div class="layui-layer-shade" id="layui-layer-shade' + a + '" times="' + a + '" style="' + ("z-index:" + (s - 1) + "; ") + '"></div>' : "", '<div class="' + l[0] + (" layui-layer-" + o.type[r.type]) + (0 != r.type && 2 != r.type || r.shade ? "" : " layui-layer-border") + " " + (r.skin || "") + '" id="' + l[0] + a + '" type="' + o.type[r.type] + '" times="' + a + '" showtime="' + r.time + '" conType="' + (e ? "object" : "string") + '" style="z-index: ' + s + "; width:" + r.area[0] + ";height:" + r.area[1] + (r.fixed ? "" : ";position:absolute;") + '">' + (e && 2 != r.type ? "" : u) + '<div id="' + (r.id || "") + '" class="layui-layer-content' + (0 == r.type && r.icon !== -1 ? " layui-layer-padding" : "") + (3 == r.type ? " layui-layer-loading" + r.icon : "") + '">' + (0 == r.type && r.icon !== -1 ? '<i class="layui-layer-ico layui-layer-ico' + r.icon + '"></i>' : "") + (1 == r.type && e ? "" : r.content || "") + '</div><span class="layui-layer-setwin">' + function () {
            var e = c ? '<a class="layui-layer-min" href="javascript:;"><cite></cite></a><a class="layui-layer-ico layui-layer-max" href="javascript:;"></a>' : "";
            return r.closeBtn && (e += '<a class="layui-layer-ico ' + l[7] + " " + l[7] + (r.title ? r.closeBtn : 4 == r.type ? "1" : "2") + '" href="javascript:;"></a>'), e
        }() + "</span>" + (r.btn ? function () {
            var e = "";
            "string" == typeof r.btn && (r.btn = [r.btn]);
            for (var t = 0, i = r.btn.length; t < i; t++) e += '<a class="' + l[6] + t + '">' + r.btn[t] + "</a>";
            return '<div class="' + l[6] + " layui-layer-btn-" + (r.btnAlign || "") + '">' + e + "</div>"
        }() : "") + (r.resize ? '<span class="layui-layer-resize"></span>' : "") + "</div>"], u, i('<div class="layui-layer-move"></div>')), n
    }, s.pt.creat = function () {
        var e = this, t = e.config, a = e.index, s = t.content, f = "object" == typeof s, c = i("body");
        if (!t.id || !i("#" + t.id)[0]) {
            switch ("string" == typeof t.area && (t.area = "auto" === t.area ? ["", ""] : [t.area, ""]), t.shift && (t.anim = t.shift), 6 == r.ie && (t.fixed = !1), t.type) {
                case 0:
                    t.btn = "btn" in t ? t.btn : o.btn[0], r.closeAll("dialog");
                    break;
                case 2:
                    var s = t.content = f ? t.content : [t.content || "", "auto"];
                    t.content = '<iframe scrolling="' + (t.content[1] || "auto") + '" allowtransparency="true" id="' + l[4] + a + '" name="' + l[4] + a + '" onload="this.className=\'\';" class="layui-layer-load" frameborder="0" src="' + t.content[0] + '"></iframe>';
                    break;
                case 3:
                    delete t.title, delete t.closeBtn, t.icon === -1 && 0 === t.icon, r.closeAll("loading");
                    break;
                case 4:
                    f || (t.content = [t.content, "body"]), t.follow = t.content[1], t.content = t.content[0] + '<i class="layui-layer-TipsG"></i>', delete t.title, t.tips = "object" == typeof t.tips ? t.tips : [t.tips, !0], t.tipsMore || r.closeAll("tips")
            }
            if (e.vessel(f, function (n, r, u) {
                c.append(n[0]), f ? function () {
                    2 == t.type || 4 == t.type ? function () {
                        i("body").append(n[1])
                    }() : function () {
                        s.parents("." + l[0])[0] || (s.data("display", s.css("display")).show().addClass("layui-layer-wrap").wrap(n[1]), i("#" + l[0] + a).find("." + l[5]).before(r))
                    }()
                }() : c.append(n[1]), i(".layui-layer-move")[0] || c.append(o.moveElem = u), e.layero = i("#" + l[0] + a), t.scrollbar || l.html.css("overflow", "hidden").attr("layer-full", a)
            }).auto(a), i("#layui-layer-shade" + e.index).css({
                "background-color": t.shade[1] || "#000",
                opacity: t.shade[0] || t.shade
            }), 2 == t.type && 6 == r.ie && e.layero.find("iframe").attr("src", s[0]), 4 == t.type ? e.tips() : e.offset(), t.fixed && n.on("resize", function () {
                e.offset(), (/^\d+%$/.test(t.area[0]) || /^\d+%$/.test(t.area[1])) && e.auto(a), 4 == t.type && e.tips()
            }), t.time <= 0 || setTimeout(function () {
                r.close(e.index)
            }, t.time), e.move().callback(), l.anim[t.anim]) {
                var u = "layer-anim " + l.anim[t.anim];
                e.layero.addClass(u).one("webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend", function () {
                    i(this).removeClass(u)
                })
            }
            t.isOutAnim && e.layero.data("isOutAnim", !0)
        }
    }, s.pt.auto = function (e) {
        var t = this, a = t.config, o = i("#" + l[0] + e);
        "" === a.area[0] && a.maxWidth > 0 && (r.ie && r.ie < 8 && a.btn && o.width(o.innerWidth()), o.outerWidth() > a.maxWidth && o.width(a.maxWidth));
        var s = [o.innerWidth(), o.innerHeight()], f = o.find(l[1]).outerHeight() || 0,
            c = o.find("." + l[6]).outerHeight() || 0, u = function (e) {
                e = o.find(e), e.height(s[1] - f - c - 2 * (0 | parseFloat(e.css("padding-top"))))
            };
        switch (a.type) {
            case 2:
                u("iframe");
                break;
            default:
                "" === a.area[1] ? a.maxHeight > 0 && o.outerHeight() > a.maxHeight ? (s[1] = a.maxHeight, u("." + l[5])) : a.fixed && s[1] >= n.height() && (s[1] = n.height(), u("." + l[5])) : u("." + l[5])
        }
        return t
    }, s.pt.offset = function () {
        var e = this, t = e.config, i = e.layero, a = [i.outerWidth(), i.outerHeight()],
            o = "object" == typeof t.offset;
        e.offsetTop = (n.height() - a[1]) / 2, e.offsetLeft = (n.width() - a[0]) / 2, o ? (e.offsetTop = t.offset[0], e.offsetLeft = t.offset[1] || e.offsetLeft) : "auto" !== t.offset && ("t" === t.offset ? e.offsetTop = 0 : "r" === t.offset ? e.offsetLeft = n.width() - a[0] : "b" === t.offset ? e.offsetTop = n.height() - a[1] : "l" === t.offset ? e.offsetLeft = 0 : "lt" === t.offset ? (e.offsetTop = 0, e.offsetLeft = 0) : "lb" === t.offset ? (e.offsetTop = n.height() - a[1], e.offsetLeft = 0) : "rt" === t.offset ? (e.offsetTop = 0, e.offsetLeft = n.width() - a[0]) : "rb" === t.offset ? (e.offsetTop = n.height() - a[1], e.offsetLeft = n.width() - a[0]) : e.offsetTop = t.offset), t.fixed || (e.offsetTop = /%$/.test(e.offsetTop) ? n.height() * parseFloat(e.offsetTop) / 100 : parseFloat(e.offsetTop), e.offsetLeft = /%$/.test(e.offsetLeft) ? n.width() * parseFloat(e.offsetLeft) / 100 : parseFloat(e.offsetLeft), e.offsetTop += n.scrollTop(), e.offsetLeft += n.scrollLeft()), i.attr("minLeft") && (e.offsetTop = n.height() - (i.find(l[1]).outerHeight() || 0), e.offsetLeft = i.css("left")), i.css({
            top: e.offsetTop,
            left: e.offsetLeft
        })
    }, s.pt.tips = function () {
        var e = this, t = e.config, a = e.layero, o = [a.outerWidth(), a.outerHeight()], r = i(t.follow);
        r[0] || (r = i("body"));
        var s = {width: r.outerWidth(), height: r.outerHeight(), top: r.offset().top, left: r.offset().left},
            f = a.find(".layui-layer-TipsG"), c = t.tips[0];
        t.tips[1] || f.remove(), s.autoLeft = function () {
            s.left + o[0] - n.width() > 0 ? (s.tipLeft = s.left + s.width - o[0], f.css({
                right: 12,
                left: "auto"
            })) : s.tipLeft = s.left
        }, s.where = [function () {
            s.autoLeft(), s.tipTop = s.top - o[1] - 10, f.removeClass("layui-layer-TipsB").addClass("layui-layer-TipsT").css("border-right-color", t.tips[1])
        }, function () {
            s.tipLeft = s.left + s.width + 10, s.tipTop = s.top, f.removeClass("layui-layer-TipsL").addClass("layui-layer-TipsR").css("border-bottom-color", t.tips[1])
        }, function () {
            s.autoLeft(), s.tipTop = s.top + s.height + 10, f.removeClass("layui-layer-TipsT").addClass("layui-layer-TipsB").css("border-right-color", t.tips[1])
        }, function () {
            s.tipLeft = s.left - o[0] - 10, s.tipTop = s.top, f.removeClass("layui-layer-TipsR").addClass("layui-layer-TipsL").css("border-bottom-color", t.tips[1])
        }], s.where[c - 1](), 1 === c ? s.top - (n.scrollTop() + o[1] + 16) < 0 && s.where[2]() : 2 === c ? n.width() - (s.left + s.width + o[0] + 16) > 0 || s.where[3]() : 3 === c ? s.top - n.scrollTop() + s.height + o[1] + 16 - n.height() > 0 && s.where[0]() : 4 === c && o[0] + 16 - s.left > 0 && s.where[1](), a.find("." + l[5]).css({
            "background-color": t.tips[1],
            "padding-right": t.closeBtn ? "30px" : ""
        }), a.css({left: s.tipLeft - (t.fixed ? n.scrollLeft() : 0), top: s.tipTop - (t.fixed ? n.scrollTop() : 0)})
    }, s.pt.move = function () {
        var e = this, t = e.config, a = i(document), s = e.layero, l = s.find(t.move),
            f = s.find(".layui-layer-resize"), c = {};
        return t.move && l.css("cursor", "move"), l.on("mousedown", function (e) {
            e.preventDefault(), t.move && (c.moveStart = !0, c.offset = [e.clientX - parseFloat(s.css("left")), e.clientY - parseFloat(s.css("top"))], o.moveElem.css("cursor", "move").show())
        }), f.on("mousedown", function (e) {
            e.preventDefault(), c.resizeStart = !0, c.offset = [e.clientX, e.clientY], c.area = [s.outerWidth(), s.outerHeight()], o.moveElem.css("cursor", "se-resize").show()
        }), a.on("mousemove", function (i) {
            if (c.moveStart) {
                var a = i.clientX - c.offset[0], o = i.clientY - c.offset[1], l = "fixed" === s.css("position");
                if (i.preventDefault(), c.stX = l ? 0 : n.scrollLeft(), c.stY = l ? 0 : n.scrollTop(), !t.moveOut) {
                    var f = n.width() - s.outerWidth() + c.stX, u = n.height() - s.outerHeight() + c.stY;
                    a < c.stX && (a = c.stX), a > f && (a = f), o < c.stY && (o = c.stY), o > u && (o = u)
                }
                s.css({left: a, top: o})
            }
            if (t.resize && c.resizeStart) {
                var a = i.clientX - c.offset[0], o = i.clientY - c.offset[1];
                i.preventDefault(), r.style(e.index, {
                    width: c.area[0] + a,
                    height: c.area[1] + o
                }), c.isResize = !0, t.resizing && t.resizing(s)
            }
        }).on("mouseup", function (e) {
            c.moveStart && (delete c.moveStart, o.moveElem.hide(), t.moveEnd && t.moveEnd(s)), c.resizeStart && (delete c.resizeStart, o.moveElem.hide())
        }), e
    }, s.pt.callback = function () {
        function e() {
            var e = a.cancel && a.cancel(t.index, n);
            e === !1 || r.close(t.index)
        }

        var t = this, n = t.layero, a = t.config;
        t.openLayer(), a.success && (2 == a.type ? n.find("iframe").on("load", function () {
            a.success(n, t.index)
        }) : a.success(n, t.index)), 6 == r.ie && t.IE6(n), n.find("." + l[6]).children("a").on("click", function () {
            var e = i(this).index();
            if (0 === e) a.yes ? a.yes(t.index, n) : a.btn1 ? a.btn1(t.index, n) : r.close(t.index); else {
                var o = a["btn" + (e + 1)] && a["btn" + (e + 1)](t.index, n);
                o === !1 || r.close(t.index)
            }
        }), n.find("." + l[7]).on("click", e), a.shadeClose && i("#layui-layer-shade" + t.index).on("click", function () {
            r.close(t.index)
        }), n.find(".layui-layer-min").on("click", function () {
            var e = a.min && a.min(n);
            e === !1 || r.min(t.index, a)
        }), n.find(".layui-layer-max").on("click", function () {
            i(this).hasClass("layui-layer-maxmin") ? (r.restore(t.index), a.restore && a.restore(n)) : (r.full(t.index, a), setTimeout(function () {
                a.full && a.full(n)
            }, 100))
        }), a.end && (o.end[t.index] = a.end)
    }, o.reselect = function () {
        i.each(i("select"), function (e, t) {
            var n = i(this);
            n.parents("." + l[0])[0] || 1 == n.attr("layer") && i("." + l[0]).length < 1 && n.removeAttr("layer").show(), n = null
        })
    }, s.pt.IE6 = function (e) {
        i("select").each(function (e, t) {
            var n = i(this);
            n.parents("." + l[0])[0] || "none" === n.css("display") || n.attr({layer: "1"}).hide(), n = null
        })
    }, s.pt.openLayer = function () {
        var e = this;
        r.zIndex = e.config.zIndex, r.setTop = function (e) {
            var t = function () {
                r.zIndex++, e.css("z-index", r.zIndex + 1)
            };
            return r.zIndex = parseInt(e[0].style.zIndex), e.on("mousedown", t), r.zIndex
        }
    }, o.record = function (e) {
        var t = [e.width(), e.height(), e.position().top, e.position().left + parseFloat(e.css("margin-left"))];
        e.find(".layui-layer-max").addClass("layui-layer-maxmin"), e.attr({area: t})
    }, o.rescollbar = function (e) {
        l.html.attr("layer-full") == e && (l.html[0].style.removeProperty ? l.html[0].style.removeProperty("overflow") : l.html[0].style.removeAttribute("overflow"), l.html.removeAttr("layer-full"))
    }, e.layer = r, r.getChildFrame = function (e, t) {
        return t = t || i("." + l[4]).attr("times"), i("#" + l[0] + t).find("iframe").contents().find(e)
    }, r.getFrameIndex = function (e) {
        return i("#" + e).parents("." + l[4]).attr("times")
    }, r.iframeAuto = function (e) {
        if (e) {
            var t = r.getChildFrame("html", e).outerHeight(), n = i("#" + l[0] + e),
                a = n.find(l[1]).outerHeight() || 0, o = n.find("." + l[6]).outerHeight() || 0;
            n.css({height: t + a + o}), n.find("iframe").css({height: t})
        }
    }, r.iframeSrc = function (e, t) {
        i("#" + l[0] + e).find("iframe").attr("src", t)
    }, r.style = function (e, t, n) {
        var a = i("#" + l[0] + e), r = a.find(".layui-layer-content"), s = a.attr("type"),
            f = a.find(l[1]).outerHeight() || 0, c = a.find("." + l[6]).outerHeight() || 0;
        a.attr("minLeft");
        s !== o.type[3] && s !== o.type[4] && (n || (parseFloat(t.width) <= 260 && (t.width = 260), parseFloat(t.height) - f - c <= 64 && (t.height = 64 + f + c)), a.css(t), c = a.find("." + l[6]).outerHeight(), s === o.type[2] ? a.find("iframe").css({height: parseFloat(t.height) - f - c}) : r.css({height: parseFloat(t.height) - f - c - parseFloat(r.css("padding-top")) - parseFloat(r.css("padding-bottom"))}))
    }, r.min = function (e, t) {
        var a = i("#" + l[0] + e), s = a.find(l[1]).outerHeight() || 0,
            f = a.attr("minLeft") || 181 * o.minIndex + "px", c = a.css("position");
        o.record(a), o.minLeft[0] && (f = o.minLeft[0], o.minLeft.shift()), a.attr("position", c), r.style(e, {
            width: 180,
            height: s,
            left: f,
            top: n.height() - s,
            position: "fixed",
            overflow: "hidden"
        }, !0), a.find(".layui-layer-min").hide(), "page" === a.attr("type") && a.find(l[4]).hide(), o.rescollbar(e), a.attr("minLeft") || o.minIndex++, a.attr("minLeft", f)
    }, r.restore = function (e) {
        var t = i("#" + l[0] + e), n = t.attr("area").split(",");
        t.attr("type");
        r.style(e, {
            width: parseFloat(n[0]),
            height: parseFloat(n[1]),
            top: parseFloat(n[2]),
            left: parseFloat(n[3]),
            position: t.attr("position"),
            overflow: "visible"
        }, !0), t.find(".layui-layer-max").removeClass("layui-layer-maxmin"), t.find(".layui-layer-min").show(), "page" === t.attr("type") && t.find(l[4]).show(), o.rescollbar(e)
    }, r.full = function (e) {
        var t, a = i("#" + l[0] + e);
        o.record(a), l.html.attr("layer-full") || l.html.css("overflow", "hidden").attr("layer-full", e), clearTimeout(t), t = setTimeout(function () {
            var t = "fixed" === a.css("position");
            r.style(e, {
                top: t ? 0 : n.scrollTop(),
                left: t ? 0 : n.scrollLeft(),
                width: n.width(),
                height: n.height()
            }, !0), a.find(".layui-layer-min").hide()
        }, 100)
    }, r.title = function (e, t) {
        var n = i("#" + l[0] + (t || r.index)).find(l[1]);
        n.html(e)
    }, r.close = function (e, t) {
        r.ready(function () {
            var n = i("#" + l[0] + e), a = n.attr("type"), s = "layer-anim-close";
            if (n[0]) {
                var f = "layui-layer-wrap", c = function () {
                    if (a === o.type[1] && "object" === n.attr("conType")) {
                        n.children(":not(." + l[5] + ")").remove();
                        for (var r = n.find("." + f), s = 0; s < 2; s++) r.unwrap();
                        r.css("display", r.data("display")).removeClass(f)
                    } else {
                        if (a === o.type[2]) try {
                            var c = i("#" + l[4] + e)[0];
                            c.contentWindow.document.write(""), c.contentWindow.close(), n.find("." + l[5])[0].removeChild(c)
                        } catch (u) {
                        }
                        n[0].innerHTML = "", n.remove()
                    }
                    "function" == typeof o.end[e] && o.end[e](), delete o.end[e], "function" == typeof t && t()
                };
                n.data("isOutAnim") && n.addClass("layer-anim " + s), i("#layui-layer-moves, #layui-layer-shade" + e).remove(), 6 == r.ie && o.reselect(), o.rescollbar(e), n.attr("minLeft") && (o.minIndex--, o.minLeft.push(n.attr("minLeft"))), r.ie && r.ie < 10 || !n.data("isOutAnim") ? c() : setTimeout(function () {
                    c()
                }, 200)
            }
        })
    }, r.closeAll = function (e, t) {
        "function" == typeof e && (t = e, e = null), r.ready(function () {
            var n = i("." + l[0]);
            i.each(n, function (a) {
                var o = i(this), s = e ? o.attr("type") === e : 1;
                s && r.close(o.attr("times"), a === n.length - 1 ? t : null), s = null
            }), 0 === n.length && "function" == typeof t && t()
        })
    };
    var f = r.cache || {}, c = function (e) {
        return f.skin ? " " + f.skin + " " + f.skin + "-" + e : ""
    };
    r.prompt = function (e, t) {
        var a = "";
        if (e = e || {}, "function" == typeof e && (t = e), e.area) {
            var o = e.area;
            a = 'style="width: ' + o[0] + "; height: " + o[1] + ';"', delete e.area
        }
        var s, l = 2 == e.formType ? '<textarea class="layui-layer-input"' + a + "></textarea>" : function () {
            return '<input type="' + (1 == e.formType ? "password" : "text") + '" class="layui-layer-input">'
        }(), f = e.success;
        return delete e.success, r.open(i.extend({
            type: 1,
            btn: ["&#x786E;&#x5B9A;", "&#x53D6;&#x6D88;"],
            content: l,
            skin: "layui-layer-prompt" + c("prompt"),
            maxWidth: n.width(),
            success: function (t) {
                s = t.find(".layui-layer-input"), s.val(e.value || "").focus(), "function" == typeof f && f(t)
            },
            resize: !1,
            yes: function (i) {
                var n = s.val();
                "" === n ? s.focus() : n.length > (e.maxlength || 500) ? r.tips("&#x6700;&#x591A;&#x8F93;&#x5165;" + (e.maxlength || 500) + "&#x4E2A;&#x5B57;&#x6570;", s, {tips: 1}) : t && t(n, i, s)
            }
        }, e))
    }, r.tab = function (e) {
        e = e || {};
        var t = e.tab || {}, n = "layui-this", a = e.success;
        return delete e.success, r.open(i.extend({
            type: 1,
            skin: "layui-layer-tab" + c("tab"),
            resize: !1,
            title: function () {
                var e = t.length, i = 1, a = "";
                if (e > 0) for (a = '<span class="' + n + '">' + t[0].title + "</span>"; i < e; i++) a += "<span>" + t[i].title + "</span>";
                return a
            }(),
            content: '<ul class="layui-layer-tabmain">' + function () {
                var e = t.length, i = 1, a = "";
                if (e > 0) for (a = '<li class="layui-layer-tabli ' + n + '">' + (t[0].content || "no content") + "</li>"; i < e; i++) a += '<li class="layui-layer-tabli">' + (t[i].content || "no  content") + "</li>";
                return a
            }() + "</ul>",
            success: function (t) {
                var o = t.find(".layui-layer-title").children(), r = t.find(".layui-layer-tabmain").children();
                o.on("mousedown", function (t) {
                    t.stopPropagation ? t.stopPropagation() : t.cancelBubble = !0;
                    var a = i(this), o = a.index();
                    a.addClass(n).siblings().removeClass(n), r.eq(o).show().siblings().hide(), "function" == typeof e.change && e.change(o)
                }), "function" == typeof a && a(t)
            }
        }, e))
    }, r.photos = function (t, n, a) {
        function o(e, t, i) {
            var n = new Image;
            return n.src = e, n.complete ? t(n) : (n.onload = function () {
                n.onload = null, t(n)
            }, void (n.onerror = function (e) {
                n.onerror = null, i(e)
            }))
        }

        var s = {};
        if (t = t || {}, t.photos) {
            var l = t.photos.constructor === Object, f = l ? t.photos : {}, u = f.data || [], d = f.start || 0;
            s.imgIndex = (0 | d) + 1, t.img = t.img || "img";
            var y = t.success;
            if (delete t.success, l) {
                if (0 === u.length) return r.msg("&#x6CA1;&#x6709;&#x56FE;&#x7247;")
            } else {
                var p = i(t.photos), h = function () {
                    u = [], p.find(t.img).each(function (e) {
                        var t = i(this);
                        t.attr("layer-index", e), u.push({
                            alt: t.attr("alt"),
                            pid: t.attr("layer-pid"),
                            src: t.attr("layer-src") || t.attr("src"),
                            thumb: t.attr("src")
                        })
                    })
                };
                if (h(), 0 === u.length) return;
                if (n || p.on("click", t.img, function () {
                    h();
                    var e = i(this), n = e.attr("layer-index");
                    r.photos(i.extend(t, {photos: {start: n, data: u, tab: t.tab}, full: t.full}), !0)
                }), !n) return
            }
            s.imgprev = function (e) {
                s.imgIndex--, s.imgIndex < 1 && (s.imgIndex = u.length), s.tabimg(e)
            }, s.imgnext = function (e, t) {
                s.imgIndex++, s.imgIndex > u.length && (s.imgIndex = 1, t) || s.tabimg(e)
            }, s.keyup = function (e) {
                if (!s.end) {
                    var t = e.keyCode;
                    e.preventDefault(), 37 === t ? s.imgprev(!0) : 39 === t ? s.imgnext(!0) : 27 === t && r.close(s.index)
                }
            }, s.tabimg = function (e) {
                if (!(u.length <= 1)) return f.start = s.imgIndex - 1, r.close(s.index), r.photos(t, !0, e)
            }, s.event = function () {
                s.bigimg.find(".layui-layer-imgprev").on("click", function (e) {
                    e.preventDefault(), s.imgprev(!0)
                }), s.bigimg.find(".layui-layer-imgnext").on("click", function (e) {
                    e.preventDefault(), s.imgnext(!0)
                }), i(document).on("keyup", s.keyup)
            }, s.loadi = r.load(1, {shade: !("shade" in t) && .9, scrollbar: !1}), o(u[d].src, function (n) {
                r.close(s.loadi), a && (t.anim = -1), s.index = r.open(i.extend({
                    type: 1,
                    id: "layui-layer-photos",
                    area: function () {
                        var a = [n.width, n.height], o = [i(e).width() - 100, i(e).height() - 100];
                        if (!t.full && (a[0] > o[0] || a[1] > o[1])) {
                            var r = [a[0] / o[0], a[1] / o[1]];
                            r[0] > r[1] ? (a[0] = a[0] / r[0], a[1] = a[1] / r[0]) : r[0] < r[1] && (a[0] = a[0] / r[1], a[1] = a[1] / r[1])
                        }
                        return [a[0] + "px", a[1] + "px"]
                    }(),
                    title: !1,
                    shade: .9,
                    shadeClose: !0,
                    closeBtn: !1,
                    move: ".layui-layer-phimg img",
                    moveType: 1,
                    scrollbar: !1,
                    moveOut: !0,
                    anim: 5,
                    isOutAnim: !1,
                    skin: "layui-layer-photos" + c("photos"),
                    content: '<div class="layui-layer-phimg"><img src="' + u[d].src + '" alt="' + (u[d].alt || "") + '" layer-pid="' + u[d].pid + '">' + function () {
                        return u.length > 1 ? '<div class="layui-layer-imgsee"><span class="layui-layer-imguide"><a href="javascript:;" class="layui-layer-iconext layui-layer-imgprev"></a><a href="javascript:;" class="layui-layer-iconext layui-layer-imgnext"></a></span><div class="layui-layer-imgbar" style="display:' + (a ? "block" : "") + '"><span class="layui-layer-imgtit"><a href="javascript:;">' + (u[d].alt || "") + "</a><em>" + s.imgIndex + " / " + u.length + "</em></span></div></div>" : ""
                    }() + "</div>",
                    success: function (e, i) {
                        s.bigimg = e.find(".layui-layer-phimg"), s.imgsee = e.find(".layui-layer-imgbar"), s.event(e), t.tab && t.tab(u[d], e), "function" == typeof y && y(e)
                    },
                    end: function () {
                        s.end = !0, i(document).off("keyup", s.keyup)
                    }
                }, t))
            }, function () {
                r.close(s.loadi), r.msg("&#x5F53;&#x524D;&#x56FE;&#x7247;&#x5730;&#x5740;&#x5F02;&#x5E38;<br>&#x662F;&#x5426;&#x7EE7;&#x7EED;&#x67E5;&#x770B;&#x4E0B;&#x4E00;&#x5F20;&#xFF1F;", {
                    time: 3e4,
                    btn: ["&#x4E0B;&#x4E00;&#x5F20;", "&#x4E0D;&#x770B;&#x4E86;"],
                    yes: function () {
                        u.length > 1 && s.imgnext(!0, !0)
                    }
                })
            })
        }
    }, o.run = function (t) {
        i = t, n = i(e), l.html = i("html"), r.open = function (e) {
            var t = new s(e);
            return t.index
        }
    }, e.layui && layui.define ? (r.ready(), layui.define("jquery", function (t) {
        r.path = layui.cache.dir, o.run(layui.$), e.layer = r, t("layer", r)
    })) : "function" == typeof define && define.amd ? define(["jquery"], function () {
        return o.run(e.jQuery), r
    }) : function () {
        r.ready(), o.run(e.jQuery)
    }()
}(window);
layui.define("jquery", function (e) {
    "use strict";
    var t = layui.$, i = {
        fixbar: function (e) {
            var i, a, n = "layui-fixbar", o = "layui-fixbar-top", r = t(document), l = t("body");
            e = t.extend({showHeight: 200}, e), e.bar1 = e.bar1 === !0 ? "&#xe606;" : e.bar1, e.bar2 = e.bar2 === !0 ? "&#xe607;" : e.bar2, e.bgcolor = e.bgcolor ? "background-color:" + e.bgcolor : "";
            var c = [e.bar1, e.bar2, "&#xe604;"],
                g = t(['<ul class="' + n + '">', e.bar1 ? '<li class="layui-icon" lay-type="bar1" style="' + e.bgcolor + '">' + c[0] + "</li>" : "", e.bar2 ? '<li class="layui-icon" lay-type="bar2" style="' + e.bgcolor + '">' + c[1] + "</li>" : "", '<li class="layui-icon ' + o + '" lay-type="top" style="' + e.bgcolor + '">' + c[2] + "</li>", "</ul>"].join("")),
                u = g.find("." + o), s = function () {
                    var t = r.scrollTop();
                    t >= e.showHeight ? i || (u.show(), i = 1) : i && (u.hide(), i = 0)
                };
            t("." + n)[0] || ("object" == typeof e.css && g.css(e.css), l.append(g), s(), g.find("li").on("click", function () {
                var i = t(this), a = i.attr("lay-type");
                "top" === a && t("html,body").animate({scrollTop: 0}, 200), e.click && e.click.call(this, a)
            }), r.on("scroll", function () {
                clearTimeout(a), a = setTimeout(function () {
                    s()
                }, 100)
            }))
        }, countdown: function (e, t, i) {
            var a = this, n = "function" == typeof t, o = new Date(e).getTime(),
                r = new Date(!t || n ? (new Date).getTime() : t).getTime(), l = o - r,
                c = [Math.floor(l / 864e5), Math.floor(l / 36e5) % 24, Math.floor(l / 6e4) % 60, Math.floor(l / 1e3) % 60];
            n && (i = t);
            var g = setTimeout(function () {
                a.countdown(e, r + 1e3, i)
            }, 1e3);
            return i && i(l > 0 ? c : [0, 0, 0, 0], t, g), l <= 0 && clearTimeout(g), g
        }, timeAgo: function (e, t) {
            var i = this, a = [[], []], n = (new Date).getTime() - new Date(e).getTime();
            return n > 26784e5 ? (n = new Date(e), a[0][0] = i.digit(n.getFullYear(), 4), a[0][1] = i.digit(n.getMonth() + 1), a[0][2] = i.digit(n.getDate()), t || (a[1][0] = i.digit(n.getHours()), a[1][1] = i.digit(n.getMinutes()), a[1][2] = i.digit(n.getSeconds())), a[0].join("-") + " " + a[1].join(":")) : n >= 864e5 ? (n / 1e3 / 60 / 60 / 24 | 0) + "天前" : n >= 36e5 ? (n / 1e3 / 60 / 60 | 0) + "小时前" : n >= 18e4 ? (n / 1e3 / 60 | 0) + "分钟前" : n < 0 ? "未来" : "刚刚"
        }, digit: function (e, t) {
            var i = "";
            e = String(e), t = t || 2;
            for (var a = e.length; a < t; a++) i += "0";
            return e < Math.pow(10, t) ? i + (0 | e) : e
        }, toDateString: function (e, t) {
            var i = this, a = new Date(e || new Date),
                n = [i.digit(a.getFullYear(), 4), i.digit(a.getMonth() + 1), i.digit(a.getDate())],
                o = [i.digit(a.getHours()), i.digit(a.getMinutes()), i.digit(a.getSeconds())];
            return t = t || "yyyy-MM-dd HH:mm:ss", t.replace(/yyyy/g, n[0]).replace(/MM/g, n[1]).replace(/dd/g, n[2]).replace(/HH/g, o[0]).replace(/mm/g, o[1]).replace(/ss/g, o[2])
        }, escape: function (e) {
            return String(e || "").replace(/&(?!#?[a-zA-Z0-9]+;)/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#39;").replace(/"/g, "&quot;")
        }, unescape: function (e) {
            return String(e || "").replace(/\&amp;/g, "&").replace(/\&lt;/g, "<").replace(/\&gt;/g, ">").replace(/\&#39;/, "'").replace(/\&quot;/, '"')
        }, event: function (e, a, n) {
            var o = t("body");
            return n = n || "click", a = i.event[e] = t.extend(!0, i.event[e], a) || {}, i.event.UTIL_EVENT_CALLBACK = i.event.UTIL_EVENT_CALLBACK || {}, o.off(n, "*[" + e + "]", i.event.UTIL_EVENT_CALLBACK[e]), i.event.UTIL_EVENT_CALLBACK[e] = function () {
                var i = t(this), n = i.attr(e);
                "function" == typeof a[n] && a[n].call(this, i)
            }, o.on(n, "*[" + e + "]", i.event.UTIL_EVENT_CALLBACK[e]), a
        }
    };
    e("util", i)
});
layui.define("jquery", function (t) {
    "use strict";
    var a = layui.$, i = (layui.hint(), layui.device()), e = "element", l = "layui-this", n = "layui-show",
        s = function () {
            this.config = {}
        };
    s.prototype.set = function (t) {
        var i = this;
        return a.extend(!0, i.config, t), i
    }, s.prototype.on = function (t, a) {
        return layui.onevent.call(this, e, t, a)
    }, s.prototype.tabAdd = function (t, i) {
        var e = ".layui-tab-title", l = a(".layui-tab[lay-filter=" + t + "]"), n = l.children(e),
            s = n.children(".layui-tab-bar"), o = l.children(".layui-tab-content"),
            r = '<li lay-id="' + (i.id || "") + '"' + (i.attr ? ' lay-attr="' + i.attr + '"' : "") + ">" + (i.title || "unnaming") + "</li>";
        return s[0] ? s.before(r) : n.append(r), o.append('<div class="layui-tab-item">' + (i.content || "") + "</div>"), f.hideTabMore(!0), f.tabAuto(), this
    }, s.prototype.tabDelete = function (t, i) {
        var e = ".layui-tab-title", l = a(".layui-tab[lay-filter=" + t + "]"), n = l.children(e),
            s = n.find('>li[lay-id="' + i + '"]');
        return f.tabDelete(null, s), this
    }, s.prototype.tabChange = function (t, i) {
        var e = ".layui-tab-title", l = a(".layui-tab[lay-filter=" + t + "]"), n = l.children(e),
            s = n.find('>li[lay-id="' + i + '"]');
        return f.tabClick.call(s[0], null, null, s), this
    }, s.prototype.tab = function (t) {
        t = t || {}, b.on("click", t.headerElem, function (i) {
            var e = a(this).index();
            f.tabClick.call(this, i, e, null, t)
        })
    }, s.prototype.progress = function (t, i) {
        var e = "layui-progress", l = a("." + e + "[lay-filter=" + t + "]"), n = l.find("." + e + "-bar"),
            s = n.find("." + e + "-text");
        return n.css("width", i), s.text(i), this
    };
    var o = ".layui-nav", r = "layui-nav-item", c = "layui-nav-bar", u = "layui-nav-tree", d = "layui-nav-child",
        y = "layui-nav-more", h = "layui-anim layui-anim-upbit", f = {
            tabClick: function (t, i, s, o) {
                o = o || {};
                var r = s || a(this), i = i || r.parent().children("li").index(r),
                    c = o.headerElem ? r.parent() : r.parents(".layui-tab").eq(0),
                    u = o.bodyElem ? a(o.bodyElem) : c.children(".layui-tab-content").children(".layui-tab-item"),
                    d = r.find("a"), y = c.attr("lay-filter");
                "javascript:;" !== d.attr("href") && "_blank" === d.attr("target") || (r.addClass(l).siblings().removeClass(l), u.eq(i).addClass(n).siblings().removeClass(n)), layui.event.call(this, e, "tab(" + y + ")", {
                    elem: c,
                    index: i
                })
            }, tabDelete: function (t, i) {
                var n = i || a(this).parent(), s = n.index(), o = n.parents(".layui-tab").eq(0),
                    r = o.children(".layui-tab-content").children(".layui-tab-item"), c = o.attr("lay-filter");
                n.hasClass(l) && (n.next()[0] ? f.tabClick.call(n.next()[0], null, s + 1) : n.prev()[0] && f.tabClick.call(n.prev()[0], null, s - 1)), n.remove(), r.eq(s).remove(), setTimeout(function () {
                    f.tabAuto()
                }, 50), layui.event.call(this, e, "tabDelete(" + c + ")", {elem: o, index: s})
            }, tabAuto: function () {
                var t = "layui-tab-more", e = "layui-tab-bar", l = "layui-tab-close", n = this;
                a(".layui-tab").each(function () {
                    var s = a(this), o = s.children(".layui-tab-title"),
                        r = (s.children(".layui-tab-content").children(".layui-tab-item"), 'lay-stope="tabmore"'),
                        c = a('<span class="layui-unselect layui-tab-bar" ' + r + "><i " + r + ' class="layui-icon">&#xe61a;</i></span>');
                    if (n === window && 8 != i.ie && f.hideTabMore(!0), s.attr("lay-allowClose") && o.find("li").each(function () {
                        var t = a(this);
                        if (!t.find("." + l)[0]) {
                            var i = a('<i class="layui-icon layui-unselect ' + l + '">&#x1006;</i>');
                            i.on("click", f.tabDelete), t.append(i)
                        }
                    }), "string" != typeof s.attr("lay-unauto")) if (o.prop("scrollWidth") > o.outerWidth() + 1) {
                        if (o.find("." + e)[0]) return;
                        o.append(c), s.attr("overflow", ""), c.on("click", function (a) {
                            o[this.title ? "removeClass" : "addClass"](t), this.title = this.title ? "" : "收缩"
                        })
                    } else o.find("." + e).remove(), s.removeAttr("overflow")
                })
            }, hideTabMore: function (t) {
                var i = a(".layui-tab-title");
                t !== !0 && "tabmore" === a(t.target).attr("lay-stope") || (i.removeClass("layui-tab-more"), i.find(".layui-tab-bar").attr("title", ""))
            }, clickThis: function () {
                var t = a(this), i = t.parents(o), n = i.attr("lay-filter"), s = t.parent(), c = t.siblings("." + d),
                    y = "string" == typeof s.attr("lay-unselect");
                "javascript:;" !== t.attr("href") && "_blank" === t.attr("target") || y || c[0] || (i.find("." + l).removeClass(l), s.addClass(l)), i.hasClass(u) && (c.removeClass(h), c[0] && (s["none" === c.css("display") ? "addClass" : "removeClass"](r + "ed"), "all" === i.attr("lay-shrink") && s.siblings().removeClass(r + "ed"))), layui.event.call(this, e, "nav(" + n + ")", t)
            }, collapse: function () {
                var t = a(this), i = t.find(".layui-colla-icon"), l = t.siblings(".layui-colla-content"),
                    s = t.parents(".layui-collapse").eq(0), o = s.attr("lay-filter"), r = "none" === l.css("display");
                if ("string" == typeof s.attr("lay-accordion")) {
                    var c = s.children(".layui-colla-item").children("." + n);
                    c.siblings(".layui-colla-title").children(".layui-colla-icon").html("&#xe602;"), c.removeClass(n)
                }
                l[r ? "addClass" : "removeClass"](n), i.html(r ? "&#xe61a;" : "&#xe602;"), layui.event.call(this, e, "collapse(" + o + ")", {
                    title: t,
                    content: l,
                    show: r
                })
            }
        };
    s.prototype.init = function (t, e) {
        var l = function () {
            return e ? '[lay-filter="' + e + '"]' : ""
        }(), s = {
            tab: function () {
                f.tabAuto.call({})
            }, nav: function () {
                var t = 200, e = {}, s = {}, p = {}, b = function (l, o, r) {
                    var c = a(this), f = c.find("." + d);
                    o.hasClass(u) ? l.css({
                        top: c.position().top,
                        height: c.children("a").outerHeight(),
                        opacity: 1
                    }) : (f.addClass(h), l.css({
                        left: c.position().left + parseFloat(c.css("marginLeft")),
                        top: c.position().top + c.height() - l.height()
                    }), e[r] = setTimeout(function () {
                        l.css({width: c.width(), opacity: 1})
                    }, i.ie && i.ie < 10 ? 0 : t), clearTimeout(p[r]), "block" === f.css("display") && clearTimeout(s[r]), s[r] = setTimeout(function () {
                        f.addClass(n), c.find("." + y).addClass(y + "d")
                    }, 300))
                };
                a(o + l).each(function (i) {
                    var l = a(this), o = a('<span class="' + c + '"></span>'), h = l.find("." + r);
                    l.find("." + c)[0] || (l.append(o), h.on("mouseenter", function () {
                        b.call(this, o, l, i)
                    }).on("mouseleave", function () {
                        l.hasClass(u) || (clearTimeout(s[i]), s[i] = setTimeout(function () {
                            l.find("." + d).removeClass(n), l.find("." + y).removeClass(y + "d")
                        }, 300))
                    }), l.on("mouseleave", function () {
                        clearTimeout(e[i]), p[i] = setTimeout(function () {
                            l.hasClass(u) ? o.css({
                                height: 0,
                                top: o.position().top + o.height() / 2,
                                opacity: 0
                            }) : o.css({width: 0, left: o.position().left + o.width() / 2, opacity: 0})
                        }, t)
                    })), h.find("a").each(function () {
                        var t = a(this), i = (t.parent(), t.siblings("." + d));
                        i[0] && !t.children("." + y)[0] && t.append('<span class="' + y + '"></span>'), t.off("click", f.clickThis).on("click", f.clickThis)
                    })
                })
            }, breadcrumb: function () {
                var t = ".layui-breadcrumb";
                a(t + l).each(function () {
                    var t = a(this), i = "lay-separator", e = t.attr(i) || "/", l = t.find("a");
                    l.next("span[" + i + "]")[0] || (l.each(function (t) {
                        t !== l.length - 1 && a(this).after("<span " + i + ">" + e + "</span>")
                    }), t.css("visibility", "visible"))
                })
            }, progress: function () {
                var t = "layui-progress";
                a("." + t + l).each(function () {
                    var i = a(this), e = i.find(".layui-progress-bar"), l = e.attr("lay-percent");
                    e.css("width", function () {
                        return /^.+\/.+$/.test(l) ? 100 * new Function("return " + l)() + "%" : l
                    }()), i.attr("lay-showPercent") && setTimeout(function () {
                        e.html('<span class="' + t + '-text">' + l + "</span>")
                    }, 350)
                })
            }, collapse: function () {
                var t = "layui-collapse";
                a("." + t + l).each(function () {
                    var t = a(this).find(".layui-colla-item");
                    t.each(function () {
                        var t = a(this), i = t.find(".layui-colla-title"), e = t.find(".layui-colla-content"),
                            l = "none" === e.css("display");
                        i.find(".layui-colla-icon").remove(), i.append('<i class="layui-icon layui-colla-icon">' + (l ? "&#xe602;" : "&#xe61a;") + "</i>"), i.off("click", f.collapse).on("click", f.collapse)
                    })
                })
            }
        };
        return s[t] ? s[t]() : layui.each(s, function (t, a) {
            a()
        })
    }, s.prototype.render = s.prototype.init;
    var p = new s, b = a(document);
    a(function () {
        p.render()
    });
    var v = ".layui-tab-title li";
    b.on("click", v, f.tabClick), b.on("click", f.hideTabMore), a(window).on("resize", f.tabAuto), t(e, p)
});
layui.define("layer", function (e) {
    "use strict";
    var t = layui.$, i = layui.layer, n = layui.hint(), o = layui.device(), a = {
            config: {}, set: function (e) {
                var i = this;
                return i.config = t.extend({}, i.config, e), i
            }, on: function (e, t) {
                return layui.onevent.call(this, r, e, t)
            }
        }, l = function () {
            var e = this;
            return {
                upload: function (t) {
                    e.upload.call(e, t)
                }, reload: function (t) {
                    e.reload.call(e, t)
                }, config: e.config
            }
        }, r = "upload", u = "layui-upload-file", c = "layui-upload-form", f = "layui-upload-iframe",
        s = "layui-upload-choose", p = function (e) {
            var i = this;
            i.config = t.extend({}, i.config, a.config, e), i.render()
        };
    p.prototype.config = {
        accept: "images",
        exts: "",
        auto: !0,
        bindAction: "",
        url: "",
        field: "file",
        acceptMime: "",
        method: "post",
        data: {},
        drag: !0,
        size: 0,
        number: 0,
        multiple: !1
    }, p.prototype.render = function (e) {
        var i = this, e = i.config;
        e.elem = t(e.elem), e.bindAction = t(e.bindAction), i.file(), i.events()
    }, p.prototype.file = function () {
        var e = this, i = e.config,
            n = e.elemFile = t(['<input class="' + u + '" type="file" accept="' + i.acceptMime + '" name="' + i.field + '"', i.multiple ? " multiple" : "", ">"].join("")),
            a = i.elem.next();
        (a.hasClass(u) || a.hasClass(c)) && a.remove(), o.ie && o.ie < 10 && i.elem.wrap('<div class="layui-upload-wrap"></div>'), e.isFile() ? (e.elemFile = i.elem, i.field = i.elem[0].name) : i.elem.after(n), o.ie && o.ie < 10 && e.initIE()
    }, p.prototype.initIE = function () {
        var e = this, i = e.config,
            n = t('<iframe id="' + f + '" class="' + f + '" name="' + f + '" frameborder="0"></iframe>'),
            o = t(['<form target="' + f + '" class="' + c + '" method="post" key="set-mine" enctype="multipart/form-data" action="' + i.url + '">', "</form>"].join(""));
        t("#" + f)[0] || t("body").append(n), i.elem.next().hasClass(c) || (e.elemFile.wrap(o), i.elem.next("." + c).append(function () {
            var e = [];
            return layui.each(i.data, function (t, i) {
                i = "function" == typeof i ? i() : i, e.push('<input type="hidden" name="' + t + '" value="' + i + '">')
            }), e.join("")
        }()))
    }, p.prototype.msg = function (e) {
        return i.msg(e, {icon: 2, shift: 6})
    }, p.prototype.isFile = function () {
        var e = this.config.elem[0];
        if (e) return "input" === e.tagName.toLocaleLowerCase() && "file" === e.type
    }, p.prototype.preview = function (e) {
        var t = this;
        window.FileReader && layui.each(t.chooseFiles, function (t, i) {
            var n = new FileReader;
            n.readAsDataURL(i), n.onload = function () {
                e && e(t, i, this.result)
            }
        })
    }, p.prototype.upload = function (e, i) {
        var n, a = this, l = a.config, r = a.elemFile[0], u = function () {
            var i = 0, n = 0, o = e || a.files || a.chooseFiles || r.files, u = function () {
                l.multiple && i + n === a.fileLength && "function" == typeof l.allDone && l.allDone({
                    total: a.fileLength,
                    successful: i,
                    aborted: n
                })
            };
            layui.each(o, function (e, o) {
                var r = new FormData;
                r.append(l.field, o), layui.each(l.data, function (e, t) {
                    t = "function" == typeof t ? t() : t, r.append(e, t)
                });
                var c = {
                    url: l.url,
                    type: "post",
                    data: r,
                    contentType: !1,
                    processData: !1,
                    dataType: "json",
                    headers: l.headers || {},
                    success: function (t) {
                        i++, d(e, t), u()
                    },
                    error: function () {
                        n++, a.msg("请求上传接口出现异常"), m(e), u()
                    }
                };
                "function" == typeof l.progress && (c.xhr = function () {
                    var e = t.ajaxSettings.xhr();
                    return e.upload.addEventListener("progress", function (e) {
                        if (e.lengthComputable) {
                            var t = Math.floor(e.loaded / e.total * 100);
                            l.progress(t, l.item ? l.item[0] : l.elem[0], e)
                        }
                    }), e
                }), t.ajax(c)
            })
        }, c = function () {
            var e = t("#" + f);
            a.elemFile.parent().submit(), clearInterval(p.timer), p.timer = setInterval(function () {
                var t, i = e.contents().find("body");
                try {
                    t = i.text()
                } catch (n) {
                    a.msg("获取上传后的响应信息出现异常"), clearInterval(p.timer), m()
                }
                t && (clearInterval(p.timer), i.html(""), d(0, t))
            }, 30)
        }, d = function (e, t) {
            if (a.elemFile.next("." + s).remove(), r.value = "", "object" != typeof t) try {
                t = JSON.parse(t)
            } catch (i) {
                return t = {}, a.msg("请对上传接口返回有效JSON")
            }
            "function" == typeof l.done && l.done(t, e || 0, function (e) {
                a.upload(e)
            })
        }, m = function (e) {
            l.auto && (r.value = ""), "function" == typeof l.error && l.error(e || 0, function (e) {
                a.upload(e)
            })
        }, h = l.exts, v = function () {
            var t = [];
            return layui.each(e || a.chooseFiles, function (e, i) {
                t.push(i.name)
            }), t
        }(), g = {
            preview: function (e) {
                a.preview(e)
            }, upload: function (e, t) {
                var i = {};
                i[e] = t, a.upload(i)
            }, pushFile: function () {
                return a.files = a.files || {}, layui.each(a.chooseFiles, function (e, t) {
                    a.files[e] = t
                }), a.files
            }, resetFile: function (e, t, i) {
                var n = new File([t], i);
                a.files = a.files || {}, a.files[e] = n
            }
        }, y = function () {
            if ("choose" !== i && !l.auto || (l.choose && l.choose(g), "choose" !== i)) return l.before && l.before(g), o.ie ? o.ie > 9 ? u() : c() : void u()
        };
        if (v = 0 === v.length ? r.value.match(/[^\/\\]+\..+/g) || [] || "" : v, 0 !== v.length) {
            switch (l.accept) {
                case"file":
                    if (h && !RegExp("\\w\\.(" + h + ")$", "i").test(escape(v))) return a.msg("选择的文件中包含不支持的格式"), r.value = "";
                    break;
                case"video":
                    if (!RegExp("\\w\\.(" + (h || "avi|mp4|wma|rmvb|rm|flash|3gp|flv") + ")$", "i").test(escape(v))) return a.msg("选择的视频中包含不支持的格式"), r.value = "";
                    break;
                case"audio":
                    if (!RegExp("\\w\\.(" + (h || "mp3|wav|mid") + ")$", "i").test(escape(v))) return a.msg("选择的音频中包含不支持的格式"), r.value = "";
                    break;
                default:
                    if (layui.each(v, function (e, t) {
                        RegExp("\\w\\.(" + (h || "jpg|png|gif|bmp|jpeg$") + ")", "i").test(escape(t)) || (n = !0)
                    }), n) return a.msg("选择的图片中包含不支持的格式"), r.value = ""
            }
            if (a.fileLength = function () {
                var t = 0, i = e || a.files || a.chooseFiles || r.files;
                return layui.each(i, function () {
                    t++
                }), t
            }(), l.number && a.fileLength > l.number) return a.msg("同时最多只能上传的数量为：" + l.number);
            if (l.size > 0 && !(o.ie && o.ie < 10)) {
                var F;
                if (layui.each(a.chooseFiles, function (e, t) {
                    if (t.size > 1024 * l.size) {
                        var i = l.size / 1024;
                        i = i >= 1 ? i.toFixed(2) + "MB" : l.size + "KB", r.value = "", F = i
                    }
                }), F) return a.msg("文件不能超过" + F)
            }
            y()
        }
    }, p.prototype.reload = function (e) {
        e = e || {}, delete e.elem, delete e.bindAction;
        var i = this, e = i.config = t.extend({}, i.config, a.config, e), n = e.elem.next();
        n.attr({name: e.name, accept: e.acceptMime, multiple: e.multiple})
    }, p.prototype.events = function () {
        var e = this, i = e.config, a = function (t) {
            e.chooseFiles = {}, layui.each(t, function (t, i) {
                var n = (new Date).getTime();
                e.chooseFiles[n + "-" + t] = i
            })
        }, l = function (t, n) {
            var o = e.elemFile,
                a = t.length > 1 ? t.length + "个文件" : (t[0] || {}).name || o[0].value.match(/[^\/\\]+\..+/g) || [] || "";
            o.next().hasClass(s) && o.next().remove(), e.upload(null, "choose"), e.isFile() || i.choose || o.after('<span class="layui-inline ' + s + '">' + a + "</span>")
        };
        i.elem.off("upload.start").on("upload.start", function () {
            var o = t(this), a = o.attr("lay-data");
            if (a) try {
                a = new Function("return " + a)(), e.config = t.extend({}, i, a)
            } catch (l) {
                n.error("Upload element property lay-data configuration item has a syntax error: " + a)
            }
            e.config.item = o, e.elemFile[0].click()
        }), o.ie && o.ie < 10 || i.elem.off("upload.over").on("upload.over", function () {
            var e = t(this);
            e.attr("lay-over", "")
        }).off("upload.leave").on("upload.leave", function () {
            var e = t(this);
            e.removeAttr("lay-over")
        }).off("upload.drop").on("upload.drop", function (n, o) {
            var r = t(this), u = o.originalEvent.dataTransfer.files || [];
            r.removeAttr("lay-over"), a(u), i.auto ? e.upload(u) : l(u)
        }), e.elemFile.off("upload.change").on("upload.change", function () {
            var t = this.files || [];
            a(t), i.auto ? e.upload() : l(t)
        }), i.bindAction.off("upload.action").on("upload.action", function () {
            e.upload()
        }), i.elem.data("haveEvents") || (e.elemFile.on("change", function () {
            t(this).trigger("upload.change")
        }), i.elem.on("click", function () {
            e.isFile() || t(this).trigger("upload.start")
        }), i.drag && i.elem.on("dragover", function (e) {
            e.preventDefault(), t(this).trigger("upload.over")
        }).on("dragleave", function (e) {
            t(this).trigger("upload.leave")
        }).on("drop", function (e) {
            e.preventDefault(), t(this).trigger("upload.drop", e)
        }), i.bindAction.on("click", function () {
            t(this).trigger("upload.action")
        }), i.elem.data("haveEvents", !0))
    }, a.render = function (e) {
        var t = new p(e);
        return l.call(t)
    }, e(r, a)
});
layui.define(["jquery", "laytpl", "lay"], function (e) {
    "use strict";
    var i = layui.$, n = layui.laytpl, t = layui.hint(), a = layui.device(), l = a.mobile ? "click" : "mousedown",
        r = "dropdown", o = "layui_" + r + "_index", u = {
            config: {}, index: layui[r] ? layui[r].index + 1e4 : 0, set: function (e) {
                var n = this;
                return n.config = i.extend({}, n.config, e), n
            }, on: function (e, i) {
                return layui.onevent.call(this, r, e, i)
            }
        }, d = function () {
            var e = this, i = e.config, n = i.id;
            return d.that[n] = e, {
                config: i, reload: function (i) {
                    e.reload.call(e, i)
                }
            }
        }, s = "layui-dropdown", m = "layui-menu-item-up", c = "layui-menu-item-down", p = "layui-menu-body-title",
        y = "layui-menu-item-group", f = "layui-menu-item-parent", v = "layui-menu-item-divider",
        g = "layui-menu-item-checked", h = "layui-menu-item-checked2", w = "layui-menu-body-panel",
        C = "layui-menu-body-panel-left", V = "." + y + ">." + p, k = function (e) {
            var n = this;
            n.index = ++u.index, n.config = i.extend({}, n.config, u.config, e), n.init()
        };
    k.prototype.config = {
        trigger: "click",
        content: "",
        className: "",
        style: "",
        show: !1,
        isAllowSpread: !0,
        isSpreadItem: !0,
        data: [],
        delay: 300
    }, k.prototype.reload = function (e) {
        var n = this;
        n.config = i.extend({}, n.config, e), n.init(!0)
    }, k.prototype.init = function (e) {
        var n = this, t = n.config, a = t.elem = i(t.elem);
        if (a.length > 1) return layui.each(a, function () {
            u.render(i.extend({}, t, {elem: this}))
        }), n;
        if (!e && a[0] && a.data(o)) {
            var l = d.getThis(a.data(o));
            if (!l) return;
            return l.reload(t)
        }
        t.id = "id" in t ? t.id : n.index, t.show && n.render(e), n.events()
    }, k.prototype.render = function (e) {
        var t = this, a = t.config, r = i("body"), s = function () {
                var e = i('<ul class="layui-menu layui-dropdown-menu"></ul>');
                return a.data.length > 0 ? m(e, a.data) : e.html('<li class="layui-menu-item-none">no menu</li>'), e
            }, m = function (e, t) {
                return layui.each(t, function (t, l) {
                    var r = l.child && l.child.length > 0, o = "isSpreadItem" in l ? l.isSpreadItem : a.isSpreadItem,
                        u = l.templet ? n(l.templet).render(l) : a.templet ? n(a.templet).render(l) : l.title,
                        d = function () {
                            return r && (l.type = l.type || "parent"), l.type ? {
                                group: "group",
                                parent: "parent",
                                "-": "-"
                            }[l.type] || "parent" : ""
                        }();
                    if ("-" === d || l.title || l.id || r) {
                        var s = i(["<li" + function () {
                            var e = {
                                group: "layui-menu-item-group" + (a.isAllowSpread ? o ? " layui-menu-item-down" : " layui-menu-item-up" : ""),
                                parent: f,
                                "-": "layui-menu-item-divider"
                            };
                            return r || d ? ' class="' + e[d] + '"' : ""
                        }() + ">", function () {
                            var e = "href" in l ? '<a href="' + l.href + '" target="' + (l.target || "_self") + '">' + u + "</a>" : u;
                            return r ? '<div class="' + p + '">' + e + function () {
                                return "parent" === d ? '<i class="layui-icon layui-icon-right"></i>' : "group" === d && a.isAllowSpread ? '<i class="layui-icon layui-icon-' + (o ? "up" : "down") + '"></i>' : ""
                            }() + "</div>" : '<div class="' + p + '">' + e + "</div>"
                        }(), "</li>"].join(""));
                        if (s.data("item", l), r) {
                            var c = i('<div class="layui-panel layui-menu-body-panel"></div>'), y = i("<ul></ul>");
                            "parent" === d ? (c.append(m(y, l.child)), s.append(c)) : s.append(m(y, l.child))
                        }
                        e.append(s)
                    }
                }), e
            },
            c = ['<div class="layui-dropdown layui-border-box layui-panel layui-anim layui-anim-downbit">', "</div>"].join("");
        ("contextmenu" === a.trigger || lay.isTopElem(a.elem[0])) && (e = !0), !e && a.elem.data(o + "_opened") || (t.elemView = i(c), t.elemView.append(a.content || s()), a.className && t.elemView.addClass(a.className), a.style && t.elemView.attr("style", a.style), u.thisId = a.id, t.remove(), r.append(t.elemView), a.elem.data(o + "_opened", !0), t.position(), d.prevElem = t.elemView, d.prevElem.data("prevElem", a.elem), t.elemView.find(".layui-menu").on(l, function (e) {
            lay.stope(e)
        }), t.elemView.find(".layui-menu li").on("click", function (e) {
            var n = i(this), l = n.data("item") || {}, r = l.child && l.child.length > 0;
            r || "-" === l.type || (t.remove(), "function" == typeof a.click && a.click(l, n))
        }), t.elemView.find(V).on("click", function (e) {
            var n = i(this), t = n.parent(), l = t.data("item") || {};
            "group" === l.type && a.isAllowSpread && d.spread(t)
        }), "mouseenter" === a.trigger && t.elemView.on("mouseenter", function () {
            clearTimeout(d.timer)
        }).on("mouseleave", function () {
            t.delayRemove()
        }))
    }, k.prototype.position = function (e) {
        var i = this, n = i.config;
        lay.position(n.elem[0], i.elemView[0], {
            position: n.position,
            e: i.e,
            clickType: "contextmenu" === n.trigger ? "right" : null
        })
    }, k.prototype.remove = function () {
        var e = this, i = (e.config, d.prevElem);
        i && (i.data("prevElem") && i.data("prevElem").data(o + "_opened", !1), i.remove())
    }, k.prototype.delayRemove = function () {
        var e = this, i = e.config;
        clearTimeout(d.timer), d.timer = setTimeout(function () {
            e.remove()
        }, i.delay)
    }, k.prototype.events = function () {
        var e = this, i = e.config;
        "hover" === i.trigger && (i.trigger = "mouseenter"), e.prevElem && e.prevElem.off(i.trigger, e.prevElemCallback), e.prevElem = i.elem, e.prevElemCallback = function (n) {
            clearTimeout(d.timer), e.e = n, e.render(), n.preventDefault(), "function" == typeof i.ready && i.ready(e.elemView, i.elem, e.e.target)
        }, i.elem.on(i.trigger, e.prevElemCallback), "mouseenter" === i.trigger && i.elem.on("mouseleave", function () {
            e.delayRemove()
        })
    }, d.that = {}, d.getThis = function (e) {
        var i = d.that[e];
        return i || t.error(e ? r + " instance with ID '" + e + "' not found" : "ID argument required"), i
    }, d.spread = function (e) {
        var i = e.children("." + p).find(".layui-icon");
        e.hasClass(m) ? (e.removeClass(m).addClass(c), i.removeClass("layui-icon-down").addClass("layui-icon-up")) : (e.removeClass(c).addClass(m), i.removeClass("layui-icon-up").addClass("layui-icon-down"))
    }, !function () {
        var e = i(window), n = i(document);
        e.on("resize", function () {
            if (u.thisId) {
                var e = d.getThis(u.thisId);
                if (e) {
                    if (!e.elemView[0] || !i("." + s)[0]) return !1;
                    var n = e.config;
                    "contextmenu" === n.trigger ? e.remove() : e.position()
                }
            }
        }), n.on(l, function (e) {
            if (u.thisId) {
                var i = d.getThis(u.thisId);
                if (i) {
                    var n = i.config;
                    !lay.isTopElem(n.elem[0]) && "contextmenu" !== n.trigger && (e.target === n.elem[0] || n.elem.find(e.target)[0] || e.target === i.elemView[0] || i.elemView && i.elemView.find(e.target)[0]) || i.remove()
                }
            }
        });
        var t = ".layui-menu:not(.layui-dropdown-menu) li";
        n.on("click", t, function (e) {
            var n = i(this), t = n.parents(".layui-menu").eq(0), a = n.hasClass(y) || n.hasClass(f),
                l = t.attr("lay-filter") || t.attr("id"), o = lay.options(this);
            n.hasClass(v) || a || (t.find("." + g).removeClass(g), t.find("." + h).removeClass(h), n.addClass(g), n.parents("." + f).addClass(h), layui.event.call(this, r, "click(" + l + ")", o))
        }), n.on("click", t + V, function (e) {
            var n = i(this), t = n.parents("." + y + ":eq(0)"), a = lay.options(t[0]);
            "isAllowSpread" in a && !a.isAllowSpread || d.spread(t)
        });
        var a = ".layui-menu ." + f;
        n.on("mouseenter", a, function (n) {
            var t = i(this), a = t.find("." + w);
            if (a[0]) {
                var l = a[0].getBoundingClientRect();
                l.right > e.width() && (a.addClass(C), l = a[0].getBoundingClientRect(), l.left < 0 && a.removeClass(C)), l.bottom > e.height() && a.eq(0).css("margin-top", -(l.bottom - e.height()))
            }
        }).on("mouseleave", a, function (e) {
            var n = i(this), t = n.children("." + w);
            t.removeClass(C), t.css("margin-top", 0)
        })
    }(), u.reload = function (e, i) {
        var n = d.getThis(e);
        return n ? (n.reload(i), d.call(n)) : this
    }, u.render = function (e) {
        var i = new k(e);
        return d.call(i)
    }, e(r, u)
});
layui.define("jquery", function (e) {
    "use strict";
    var i = layui.jquery, t = {
            config: {}, index: layui.slider ? layui.slider.index + 1e4 : 0, set: function (e) {
                var t = this;
                return t.config = i.extend({}, t.config, e), t
            }, on: function (e, i) {
                return layui.onevent.call(this, n, e, i)
            }
        }, a = function () {
            var e = this, i = e.config;
            return {
                setValue: function (t, a) {
                    return i.value = t, e.slide("set", t, a || 0)
                }, config: i
            }
        }, n = "slider", l = "layui-disabled", s = "layui-slider", r = "layui-slider-bar", o = "layui-slider-wrap",
        u = "layui-slider-wrap-btn", d = "layui-slider-tips", v = "layui-slider-input", c = "layui-slider-input-txt",
        p = "layui-slider-input-btn", m = "layui-slider-hover", f = function (e) {
            var a = this;
            a.index = ++t.index, a.config = i.extend({}, a.config, t.config, e), a.render()
        };
    f.prototype.config = {
        type: "default",
        min: 0,
        max: 100,
        value: 0,
        step: 1,
        showstep: !1,
        tips: !0,
        input: !1,
        range: !1,
        height: 200,
        disabled: !1,
        theme: "#009688"
    }, f.prototype.render = function () {
        var e = this, t = e.config;
        if (t.step < 1 && (t.step = 1), t.max < t.min && (t.max = t.min + t.step), t.range) {
            t.value = "object" == typeof t.value ? t.value : [t.min, t.value];
            var a = Math.min(t.value[0], t.value[1]), n = Math.max(t.value[0], t.value[1]);
            t.value[0] = a > t.min ? a : t.min, t.value[1] = n > t.min ? n : t.min, t.value[0] = t.value[0] > t.max ? t.max : t.value[0], t.value[1] = t.value[1] > t.max ? t.max : t.value[1];
            var r = Math.floor((t.value[0] - t.min) / (t.max - t.min) * 100),
                v = Math.floor((t.value[1] - t.min) / (t.max - t.min) * 100), p = v - r + "%";
            r += "%", v += "%"
        } else {
            "object" == typeof t.value && (t.value = Math.min.apply(null, t.value)), t.value < t.min && (t.value = t.min), t.value > t.max && (t.value = t.max);
            var p = Math.floor((t.value - t.min) / (t.max - t.min) * 100) + "%"
        }
        var m = t.disabled ? "#c2c2c2" : t.theme,
            f = '<div class="layui-slider ' + ("vertical" === t.type ? "layui-slider-vertical" : "") + '">' + (t.tips ? '<div class="layui-slider-tips"></div>' : "") + '<div class="layui-slider-bar" style="background:' + m + "; " + ("vertical" === t.type ? "height" : "width") + ":" + p + ";" + ("vertical" === t.type ? "bottom" : "left") + ":" + (r || 0) + ';"></div><div class="layui-slider-wrap" style="' + ("vertical" === t.type ? "bottom" : "left") + ":" + (r || p) + ';"><div class="layui-slider-wrap-btn" style="border: 2px solid ' + m + ';"></div></div>' + (t.range ? '<div class="layui-slider-wrap" style="' + ("vertical" === t.type ? "bottom" : "left") + ":" + v + ';"><div class="layui-slider-wrap-btn" style="border: 2px solid ' + m + ';"></div></div>' : "") + "</div>",
            h = i(t.elem), y = h.next("." + s);
        if (y[0] && y.remove(), e.elemTemp = i(f), t.range ? (e.elemTemp.find("." + o).eq(0).data("value", t.value[0]), e.elemTemp.find("." + o).eq(1).data("value", t.value[1])) : e.elemTemp.find("." + o).data("value", t.value), h.html(e.elemTemp), "vertical" === t.type && e.elemTemp.height(t.height + "px"), t.showstep) {
            for (var g = (t.max - t.min) / t.step, b = "", x = 1; x < g + 1; x++) {
                var T = 100 * x / g;
                T < 100 && (b += '<div class="layui-slider-step" style="' + ("vertical" === t.type ? "bottom" : "left") + ":" + T + '%"></div>')
            }
            e.elemTemp.append(b)
        }
        if (t.input && !t.range) {
            var w = i('<div class="layui-slider-input layui-input"><div class="layui-slider-input-txt"><input type="text" class="layui-input"></div><div class="layui-slider-input-btn"><i class="layui-icon layui-icon-up"></i><i class="layui-icon layui-icon-down"></i></div></div>');
            h.css("position", "relative"), h.append(w), h.find("." + c).children("input").val(t.value), "vertical" === t.type ? w.css({
                left: 0,
                top: -48
            }) : e.elemTemp.css("margin-right", w.outerWidth() + 15)
        }
        t.disabled ? (e.elemTemp.addClass(l), e.elemTemp.find("." + u).addClass(l)) : e.slide(), e.elemTemp.find("." + u).on("mouseover", function () {
            var a = "vertical" === t.type ? t.height : e.elemTemp[0].offsetWidth, n = e.elemTemp.find("." + o),
                l = "vertical" === t.type ? a - i(this).parent()[0].offsetTop - n.height() : i(this).parent()[0].offsetLeft,
                s = l / a * 100, r = i(this).parent().data("value"), u = t.setTips ? t.setTips(r) : r;
            e.elemTemp.find("." + d).html(u), "vertical" === t.type ? e.elemTemp.find("." + d).css({
                bottom: s + "%",
                "margin-bottom": "20px",
                display: "inline-block"
            }) : e.elemTemp.find("." + d).css({left: s + "%", display: "inline-block"})
        }).on("mouseout", function () {
            e.elemTemp.find("." + d).css("display", "none")
        })
    }, f.prototype.slide = function (e, t, a) {
        var n = this, l = n.config, s = n.elemTemp, f = function () {
                return "vertical" === l.type ? l.height : s[0].offsetWidth
            }, h = s.find("." + o), y = s.next("." + v), g = y.children("." + c).children("input").val(),
            b = 100 / ((l.max - l.min) / Math.ceil(l.step)), x = function (e, i) {
                e = Math.ceil(e) * b > 100 ? Math.ceil(e) * b : Math.round(e) * b, e = e > 100 ? 100 : e, h.eq(i).css("vertical" === l.type ? "bottom" : "left", e + "%");
                var t = T(h[0].offsetLeft), a = l.range ? T(h[1].offsetLeft) : 0;
                "vertical" === l.type ? (s.find("." + d).css({
                    bottom: e + "%",
                    "margin-bottom": "20px"
                }), t = T(f() - h[0].offsetTop - h.height()), a = l.range ? T(f() - h[1].offsetTop - h.height()) : 0) : s.find("." + d).css("left", e + "%"), t = t > 100 ? 100 : t, a = a > 100 ? 100 : a;
                var n = Math.min(t, a), o = Math.abs(t - a);
                "vertical" === l.type ? s.find("." + r).css({
                    height: o + "%",
                    bottom: n + "%"
                }) : s.find("." + r).css({width: o + "%", left: n + "%"});
                var u = l.min + Math.round((l.max - l.min) * e / 100);
                if (g = u, y.children("." + c).children("input").val(g), h.eq(i).data("value", u), s.find("." + d).html(l.setTips ? l.setTips(u) : u), l.range) {
                    var v = [h.eq(0).data("value"), h.eq(1).data("value")];
                    v[0] > v[1] && v.reverse()
                }
                l.change && l.change(l.range ? v : u)
            }, T = function (e) {
                var i = e / f() * 100 / b, t = Math.round(i) * b;
                return e == f() && (t = Math.ceil(i) * b), t
            }, w = i(['<div class="layui-auxiliar-moving" id="LAY-slider-moving"></div'].join("")), M = function (e, t) {
                var a = function () {
                    t && t(), w.remove()
                };
                i("#LAY-slider-moving")[0] || i("body").append(w), w.on("mousemove", e), w.on("mouseup", a).on("mouseleave", a)
            };
        if ("set" === e) return x(t, a);
        s.find("." + u).each(function (e) {
            var t = i(this);
            t.on("mousedown", function (i) {
                i = i || window.event;
                var a = t.parent()[0].offsetLeft, n = i.clientX;
                "vertical" === l.type && (a = f() - t.parent()[0].offsetTop - h.height(), n = i.clientY);
                var r = function (i) {
                    i = i || window.event;
                    var r = a + ("vertical" === l.type ? n - i.clientY : i.clientX - n);
                    r < 0 && (r = 0), r > f() && (r = f());
                    var o = r / f() * 100 / b;
                    x(o, e), t.addClass(m), s.find("." + d).show(), i.preventDefault()
                }, o = function () {
                    t.removeClass(m), s.find("." + d).hide()
                };
                M(r, o)
            })
        }), s.on("click", function (e) {
            var t = i("." + u);
            if (!t.is(event.target) && 0 === t.has(event.target).length && t.length) {
                var a,
                    n = "vertical" === l.type ? f() - e.clientY + i(this).offset().top : e.clientX - i(this).offset().left;
                n < 0 && (n = 0), n > f() && (n = f());
                var s = n / f() * 100 / b;
                a = l.range ? "vertical" === l.type ? Math.abs(n - parseInt(i(h[0]).css("bottom"))) > Math.abs(n - parseInt(i(h[1]).css("bottom"))) ? 1 : 0 : Math.abs(n - h[0].offsetLeft) > Math.abs(n - h[1].offsetLeft) ? 1 : 0 : 0, x(s, a), e.preventDefault()
            }
        }), y.children("." + p).children("i").each(function (e) {
            i(this).on("click", function () {
                g = y.children("." + c).children("input").val(), g = 1 == e ? g - l.step < l.min ? l.min : Number(g) - l.step : Number(g) + l.step > l.max ? l.max : Number(g) + l.step;
                var i = (g - l.min) / (l.max - l.min) * 100 / b;
                x(i, 0)
            })
        });
        var q = function () {
            var e = this.value;
            e = isNaN(e) ? 0 : e, e = e < l.min ? l.min : e, e = e > l.max ? l.max : e, this.value = e;
            var i = (e - l.min) / (l.max - l.min) * 100 / b;
            x(i, 0)
        };
        y.children("." + c).children("input").on("keydown", function (e) {
            13 === e.keyCode && (e.preventDefault(), q.call(this))
        }).on("change", q)
    }, f.prototype.events = function () {
        var e = this;
        e.config
    }, t.render = function (e) {
        var i = new f(e);
        return a.call(i)
    }, e(n, t)
});
layui.define("jquery", function (e) {
    "use strict";
    var i = layui.jquery, o = {
            config: {}, index: layui.colorpicker ? layui.colorpicker.index + 1e4 : 0, set: function (e) {
                var o = this;
                return o.config = i.extend({}, o.config, e), o
            }, on: function (e, i) {
                return layui.onevent.call(this, "colorpicker", e, i)
            }
        }, r = function () {
            var e = this, i = e.config;
            return {config: i}
        }, t = "colorpicker", n = "layui-show", l = "layui-colorpicker", c = ".layui-colorpicker-main",
        a = "layui-icon-down", s = "layui-icon-close", f = "layui-colorpicker-trigger-span",
        d = "layui-colorpicker-trigger-i", u = "layui-colorpicker-side", p = "layui-colorpicker-side-slider",
        g = "layui-colorpicker-basis", v = "layui-colorpicker-alpha-bgcolor", h = "layui-colorpicker-alpha-slider",
        m = "layui-colorpicker-basis-cursor", b = "layui-colorpicker-main-input", k = function (e) {
            var i = {h: 0, s: 0, b: 0}, o = Math.min(e.r, e.g, e.b), r = Math.max(e.r, e.g, e.b), t = r - o;
            return i.b = r, i.s = 0 != r ? 255 * t / r : 0, 0 != i.s ? e.r == r ? i.h = (e.g - e.b) / t : e.g == r ? i.h = 2 + (e.b - e.r) / t : i.h = 4 + (e.r - e.g) / t : i.h = -1, r == o && (i.h = 0), i.h *= 60, i.h < 0 && (i.h += 360), i.s *= 100 / 255, i.b *= 100 / 255, i
        }, y = function (e) {
            var e = e.indexOf("#") > -1 ? e.substring(1) : e;
            if (3 == e.length) {
                var i = e.split("");
                e = i[0] + i[0] + i[1] + i[1] + i[2] + i[2]
            }
            e = parseInt(e, 16);
            var o = {r: e >> 16, g: (65280 & e) >> 8, b: 255 & e};
            return k(o)
        }, x = function (e) {
            var i = {}, o = e.h, r = 255 * e.s / 100, t = 255 * e.b / 100;
            if (0 == r) i.r = i.g = i.b = t; else {
                var n = t, l = (255 - r) * t / 255, c = (n - l) * (o % 60) / 60;
                360 == o && (o = 0), o < 60 ? (i.r = n, i.b = l, i.g = l + c) : o < 120 ? (i.g = n, i.b = l, i.r = n - c) : o < 180 ? (i.g = n, i.r = l, i.b = l + c) : o < 240 ? (i.b = n, i.r = l, i.g = n - c) : o < 300 ? (i.b = n, i.g = l, i.r = l + c) : o < 360 ? (i.r = n, i.g = l, i.b = n - c) : (i.r = 0, i.g = 0, i.b = 0)
            }
            return {r: Math.round(i.r), g: Math.round(i.g), b: Math.round(i.b)}
        }, C = function (e) {
            var o = x(e), r = [o.r.toString(16), o.g.toString(16), o.b.toString(16)];
            return i.each(r, function (e, i) {
                1 == i.length && (r[e] = "0" + i)
            }), r.join("")
        }, P = function (e) {
            var i = /[0-9]{1,3}/g, o = e.match(i) || [];
            return {r: o[0], g: o[1], b: o[2]}
        }, B = i(window), w = i(document), D = function (e) {
            var r = this;
            r.index = ++o.index, r.config = i.extend({}, r.config, o.config, e), r.render()
        };
    D.prototype.config = {
        color: "",
        size: null,
        alpha: !1,
        format: "hex",
        predefine: !1,
        colors: ["#009688", "#5FB878", "#1E9FFF", "#FF5722", "#FFB800", "#01AAED", "#999", "#c00", "#ff8c00", "#ffd700", "#90ee90", "#00ced1", "#1e90ff", "#c71585", "rgb(0, 186, 189)", "rgb(255, 120, 0)", "rgb(250, 212, 0)", "#393D49", "rgba(0,0,0,.5)", "rgba(255, 69, 0, 0.68)", "rgba(144, 240, 144, 0.5)", "rgba(31, 147, 255, 0.73)"]
    }, D.prototype.render = function () {
        var e = this, o = e.config,
            r = i(['<div class="layui-unselect layui-colorpicker">', "<span " + ("rgb" == o.format && o.alpha ? 'class="layui-colorpicker-trigger-bgcolor"' : "") + ">", '<span class="layui-colorpicker-trigger-span" ', 'lay-type="' + ("rgb" == o.format ? o.alpha ? "rgba" : "torgb" : "") + '" ', 'style="' + function () {
                var e = "";
                return o.color ? (e = o.color, (o.color.match(/[0-9]{1,3}/g) || []).length > 3 && (o.alpha && "rgb" == o.format || (e = "#" + C(k(P(o.color))))), "background: " + e) : e
            }() + '">', '<i class="layui-icon layui-colorpicker-trigger-i ' + (o.color ? a : s) + '"></i>', "</span>", "</span>", "</div>"].join("")),
            t = i(o.elem);
        o.size && r.addClass("layui-colorpicker-" + o.size), t.addClass("layui-inline").html(e.elemColorBox = r), e.color = e.elemColorBox.find("." + f)[0].style.background, e.events()
    }, D.prototype.renderPicker = function () {
        var e = this, o = e.config, r = e.elemColorBox[0],
            t = e.elemPicker = i(['<div id="layui-colorpicker' + e.index + '" data-index="' + e.index + '" class="layui-anim layui-anim-upbit layui-colorpicker-main">', '<div class="layui-colorpicker-main-wrapper">', '<div class="layui-colorpicker-basis">', '<div class="layui-colorpicker-basis-white"></div>', '<div class="layui-colorpicker-basis-black"></div>', '<div class="layui-colorpicker-basis-cursor"></div>', "</div>", '<div class="layui-colorpicker-side">', '<div class="layui-colorpicker-side-slider"></div>', "</div>", "</div>", '<div class="layui-colorpicker-main-alpha ' + (o.alpha ? n : "") + '">', '<div class="layui-colorpicker-alpha-bgcolor">', '<div class="layui-colorpicker-alpha-slider"></div>', "</div>", "</div>", function () {
                if (o.predefine) {
                    var e = ['<div class="layui-colorpicker-main-pre">'];
                    return layui.each(o.colors, function (i, o) {
                        e.push(['<div class="layui-colorpicker-pre' + ((o.match(/[0-9]{1,3}/g) || []).length > 3 ? " layui-colorpicker-pre-isalpha" : "") + '">', '<div style="background:' + o + '"></div>', "</div>"].join(""))
                    }), e.push("</div>"), e.join("")
                }
                return ""
            }(), '<div class="layui-colorpicker-main-input">', '<div class="layui-inline">', '<input type="text" class="layui-input">', "</div>", '<div class="layui-btn-container">', '<button class="layui-btn layui-btn-primary layui-btn-sm" colorpicker-events="clear">清空</button>', '<button class="layui-btn layui-btn-sm" colorpicker-events="confirm">确定</button>', "</div", "</div>", "</div>"].join(""));
        e.elemColorBox.find("." + f)[0];
        i(c)[0] && i(c).data("index") == e.index ? e.removePicker(D.thisElemInd) : (e.removePicker(D.thisElemInd), i("body").append(t)), D.thisElemInd = e.index, D.thisColor = r.style.background, e.position(), e.pickerEvents()
    }, D.prototype.removePicker = function (e) {
        var o = this;
        o.config;
        return i("#layui-colorpicker" + (e || o.index)).remove(), o
    }, D.prototype.position = function () {
        var e = this, i = e.config, o = e.bindElem || e.elemColorBox[0], r = e.elemPicker[0],
            t = o.getBoundingClientRect(), n = r.offsetWidth, l = r.offsetHeight, c = function (e) {
                return e = e ? "scrollLeft" : "scrollTop", document.body[e] | document.documentElement[e]
            }, a = function (e) {
                return document.documentElement[e ? "clientWidth" : "clientHeight"]
            }, s = 5, f = t.left, d = t.bottom;
        f -= (n - o.offsetWidth) / 2, d += s, f + n + s > a("width") ? f = a("width") - n - s : f < s && (f = s), d + l + s > a() && (d = t.top > l ? t.top - l : a() - l, d -= 2 * s), i.position && (r.style.position = i.position), r.style.left = f + ("fixed" === i.position ? 0 : c(1)) + "px", r.style.top = d + ("fixed" === i.position ? 0 : c()) + "px"
    }, D.prototype.val = function () {
        var e = this, i = (e.config, e.elemColorBox.find("." + f)), o = e.elemPicker.find("." + b), r = i[0],
            t = r.style.backgroundColor;
        if (t) {
            var n = k(P(t)), l = i.attr("lay-type");
            if (e.select(n.h, n.s, n.b), "torgb" === l && o.find("input").val(t), "rgba" === l) {
                var c = P(t);
                if (3 == (t.match(/[0-9]{1,3}/g) || []).length) o.find("input").val("rgba(" + c.r + ", " + c.g + ", " + c.b + ", 1)"), e.elemPicker.find("." + h).css("left", 280); else {
                    o.find("input").val(t);
                    var a = 280 * t.slice(t.lastIndexOf(",") + 1, t.length - 1);
                    e.elemPicker.find("." + h).css("left", a)
                }
                e.elemPicker.find("." + v)[0].style.background = "linear-gradient(to right, rgba(" + c.r + ", " + c.g + ", " + c.b + ", 0), rgb(" + c.r + ", " + c.g + ", " + c.b + "))"
            }
        } else e.select(0, 100, 100), o.find("input").val(""), e.elemPicker.find("." + v)[0].style.background = "", e.elemPicker.find("." + h).css("left", 280)
    }, D.prototype.side = function () {
        var e = this, o = e.config, r = e.elemColorBox.find("." + f), t = r.attr("lay-type"),
            n = e.elemPicker.find("." + u), l = e.elemPicker.find("." + p), c = e.elemPicker.find("." + g),
            y = e.elemPicker.find("." + m), C = e.elemPicker.find("." + v), w = e.elemPicker.find("." + h),
            D = l[0].offsetTop / 180 * 360, E = 100 - (y[0].offsetTop + 3) / 180 * 100,
            H = (y[0].offsetLeft + 3) / 260 * 100, W = Math.round(w[0].offsetLeft / 280 * 100) / 100,
            j = e.elemColorBox.find("." + d), F = e.elemPicker.find(".layui-colorpicker-pre").children("div"),
            L = function (i, n, l, c) {
                e.select(i, n, l);
                var f = x({h: i, s: n, b: l});
                if (j.addClass(a).removeClass(s), r[0].style.background = "rgb(" + f.r + ", " + f.g + ", " + f.b + ")", "torgb" === t && e.elemPicker.find("." + b).find("input").val("rgb(" + f.r + ", " + f.g + ", " + f.b + ")"), "rgba" === t) {
                    var d = 0;
                    d = 280 * c, w.css("left", d), e.elemPicker.find("." + b).find("input").val("rgba(" + f.r + ", " + f.g + ", " + f.b + ", " + c + ")"), r[0].style.background = "rgba(" + f.r + ", " + f.g + ", " + f.b + ", " + c + ")", C[0].style.background = "linear-gradient(to right, rgba(" + f.r + ", " + f.g + ", " + f.b + ", 0), rgb(" + f.r + ", " + f.g + ", " + f.b + "))"
                }
                o.change && o.change(e.elemPicker.find("." + b).find("input").val())
            }, M = i(['<div class="layui-auxiliar-moving" id="LAY-colorpicker-moving"></div'].join("")),
            Y = function (e) {
                i("#LAY-colorpicker-moving")[0] || i("body").append(M), M.on("mousemove", e), M.on("mouseup", function () {
                    M.remove()
                }).on("mouseleave", function () {
                    M.remove()
                })
            };
        l.on("mousedown", function (e) {
            var i = this.offsetTop, o = e.clientY, r = function (e) {
                var r = i + (e.clientY - o), t = n[0].offsetHeight;
                r < 0 && (r = 0), r > t && (r = t);
                var l = r / 180 * 360;
                D = l, L(l, H, E, W), e.preventDefault()
            };
            Y(r), e.preventDefault()
        }), n.on("click", function (e) {
            var o = e.clientY - i(this).offset().top;
            o < 0 && (o = 0), o > this.offsetHeight && (o = this.offsetHeight);
            var r = o / 180 * 360;
            D = r, L(r, H, E, W), e.preventDefault()
        }), y.on("mousedown", function (e) {
            var i = this.offsetTop, o = this.offsetLeft, r = e.clientY, t = e.clientX, n = function (e) {
                var n = i + (e.clientY - r), l = o + (e.clientX - t), a = c[0].offsetHeight - 3,
                    s = c[0].offsetWidth - 3;
                n < -3 && (n = -3), n > a && (n = a), l < -3 && (l = -3), l > s && (l = s);
                var f = (l + 3) / 260 * 100, d = 100 - (n + 3) / 180 * 100;
                E = d, H = f, L(D, f, d, W), e.preventDefault()
            };
            layui.stope(e), Y(n), e.preventDefault()
        }), c.on("mousedown", function (e) {
            var o = e.clientY - i(this).offset().top - 3 + B.scrollTop(),
                r = e.clientX - i(this).offset().left - 3 + B.scrollLeft();
            o < -3 && (o = -3), o > this.offsetHeight - 3 && (o = this.offsetHeight - 3), r < -3 && (r = -3), r > this.offsetWidth - 3 && (r = this.offsetWidth - 3);
            var t = (r + 3) / 260 * 100, n = 100 - (o + 3) / 180 * 100;
            E = n, H = t, L(D, t, n, W), e.preventDefault(), y.trigger(e, "mousedown")
        }), w.on("mousedown", function (e) {
            var i = this.offsetLeft, o = e.clientX, r = function (e) {
                var r = i + (e.clientX - o), t = C[0].offsetWidth;
                r < 0 && (r = 0), r > t && (r = t);
                var n = Math.round(r / 280 * 100) / 100;
                W = n, L(D, H, E, n), e.preventDefault()
            };
            Y(r), e.preventDefault()
        }), C.on("click", function (e) {
            var o = e.clientX - i(this).offset().left;
            o < 0 && (o = 0), o > this.offsetWidth && (o = this.offsetWidth);
            var r = Math.round(o / 280 * 100) / 100;
            W = r, L(D, H, E, r), e.preventDefault()
        }), F.each(function () {
            i(this).on("click", function () {
                i(this).parent(".layui-colorpicker-pre").addClass("selected").siblings().removeClass("selected");
                var e, o = this.style.backgroundColor, r = k(P(o)), t = o.slice(o.lastIndexOf(",") + 1, o.length - 1);
                D = r.h, H = r.s, E = r.b, 3 == (o.match(/[0-9]{1,3}/g) || []).length && (t = 1), W = t, e = 280 * t, L(r.h, r.s, r.b, t)
            })
        })
    }, D.prototype.select = function (e, i, o, r) {
        var t = this, n = (t.config, C({h: e, s: 100, b: 100})), l = C({h: e, s: i, b: o}), c = e / 360 * 180,
            a = 180 - o / 100 * 180 - 3, s = i / 100 * 260 - 3;
        t.elemPicker.find("." + p).css("top", c), t.elemPicker.find("." + g)[0].style.background = "#" + n, t.elemPicker.find("." + m).css({
            top: a,
            left: s
        }), "change" !== r && t.elemPicker.find("." + b).find("input").val("#" + l)
    }, D.prototype.pickerEvents = function () {
        var e = this, o = e.config, r = e.elemColorBox.find("." + f), t = e.elemPicker.find("." + b + " input"), n = {
            clear: function (i) {
                r[0].style.background = "", e.elemColorBox.find("." + d).removeClass(a).addClass(s), e.color = "", o.done && o.done(""), e.removePicker()
            }, confirm: function (i, n) {
                var l = t.val(), c = l, f = {};
                if (l.indexOf(",") > -1) {
                    if (f = k(P(l)), e.select(f.h, f.s, f.b), r[0].style.background = c = "#" + C(f), (l.match(/[0-9]{1,3}/g) || []).length > 3 && "rgba" === r.attr("lay-type")) {
                        var u = 280 * l.slice(l.lastIndexOf(",") + 1, l.length - 1);
                        e.elemPicker.find("." + h).css("left", u), r[0].style.background = l, c = l
                    }
                } else f = y(l), r[0].style.background = c = "#" + C(f), e.elemColorBox.find("." + d).removeClass(s).addClass(a);
                return "change" === n ? (e.select(f.h, f.s, f.b, n), void (o.change && o.change(c))) : (e.color = l, o.done && o.done(l), void e.removePicker())
            }
        };
        e.elemPicker.on("click", "*[colorpicker-events]", function () {
            var e = i(this), o = e.attr("colorpicker-events");
            n[o] && n[o].call(this, e)
        }), t.on("keyup", function (e) {
            var o = i(this);
            n.confirm.call(this, o, 13 === e.keyCode ? null : "change")
        })
    }, D.prototype.events = function () {
        var e = this, o = e.config, r = e.elemColorBox.find("." + f);
        e.elemColorBox.on("click", function () {
            e.renderPicker(), i(c)[0] && (e.val(), e.side())
        }), o.elem[0] && !e.elemColorBox[0].eventHandler && (w.on("click", function (o) {
            if (!i(o.target).hasClass(l) && !i(o.target).parents("." + l)[0] && !i(o.target).hasClass(c.replace(/\./g, "")) && !i(o.target).parents(c)[0] && e.elemPicker) {
                if (e.color) {
                    var t = k(P(e.color));
                    e.select(t.h, t.s, t.b)
                } else e.elemColorBox.find("." + d).removeClass(a).addClass(s);
                r[0].style.background = e.color || "", e.removePicker()
            }
        }), B.on("resize", function () {
            return !(!e.elemPicker || !i(c)[0]) && void e.position()
        }), e.elemColorBox[0].eventHandler = !0)
    }, o.render = function (e) {
        var i = new D(e);
        return r.call(i)
    }, e(t, o)
});
layui.define("layer", function (e) {
    "use strict";
    var t = layui.$, i = layui.layer, a = layui.hint(), n = layui.device(), l = "form", r = ".layui-form",
        s = "layui-this", o = "layui-hide", c = "layui-disabled", u = function () {
            this.config = {
                verify: {
                    required: [/[\S]+/, "必填项不能为空"],
                    phone: [/^1\d{10}$/, "请输入正确的手机号"],
                    email: [/^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/, "邮箱格式不正确"],
                    url: [/(^#)|(^http(s*):\/\/[^\s]+\.[^\s]+)/, "链接格式不正确"],
                    number: function (e) {
                        if (!e || isNaN(e)) return "只能填写数字"
                    },
                    date: [/^(\d{4})[-\/](\d{1}|0\d{1}|1[0-2])([-\/](\d{1}|0\d{1}|[1-2][0-9]|3[0-1]))*$/, "日期格式不正确"],
                    identity: [/(^\d{15}$)|(^\d{17}(x|X|\d)$)/, "请输入正确的身份证号"]
                }
            }
        };
    u.prototype.set = function (e) {
        var i = this;
        return t.extend(!0, i.config, e), i
    }, u.prototype.verify = function (e) {
        var i = this;
        return t.extend(!0, i.config.verify, e), i
    }, u.prototype.on = function (e, t) {
        return layui.onevent.call(this, l, e, t)
    }, u.prototype.val = function (e, i) {
        var a = this, n = t(r + '[lay-filter="' + e + '"]');
        return n.each(function (e, a) {
            var n = t(this);
            layui.each(i, function (e, t) {
                var i, a = n.find('[name="' + e + '"]');
                a[0] && (i = a[0].type, "checkbox" === i ? a[0].checked = t : "radio" === i ? a.each(function () {
                    this.value == t && (this.checked = !0)
                }) : a.val(t))
            })
        }), f.render(null, e), a.getValue(e)
    }, u.prototype.getValue = function (e, i) {
        i = i || t(r + '[lay-filter="' + e + '"]').eq(0);
        var a = {}, n = {}, l = i.find("input,select,textarea");
        return layui.each(l, function (e, i) {
            var l;
            t(this);
            if (i.name = (i.name || "").replace(/^\s*|\s*&/, ""), i.name) {
                if (/^.*\[\]$/.test(i.name)) {
                    var r = i.name.match(/^(.*)\[\]$/g)[0];
                    a[r] = 0 | a[r], l = i.name.replace(/^(.*)\[\]$/, "$1[" + a[r]++ + "]")
                }
                /^checkbox|radio$/.test(i.type) && !i.checked || (n[l || i.name] = i.value)
            }
        }), n
    }, u.prototype.render = function (e, i) {
        var n = this, u = t(r + function () {
            return i ? '[lay-filter="' + i + '"]' : ""
        }()), d = {
            select: function () {
                var e, i = "请选择", a = "layui-form-select", n = "layui-select-title", r = "layui-select-none", d = "",
                    f = u.find("select"), v = function (i, l) {
                        t(i.target).parent().hasClass(n) && !l || (t("." + a).removeClass(a + "ed " + a + "up"), e && d && e.val(d)), e = null
                    }, y = function (i, u, f) {
                        var y, p = t(this), m = i.find("." + n), k = m.find("input"), g = i.find("dl"),
                            x = g.children("dd"), b = this.selectedIndex;
                        if (!u) {
                            var C = function () {
                                var e = i.offset().top + i.outerHeight() + 5 - h.scrollTop(), t = g.outerHeight();
                                b = p[0].selectedIndex, i.addClass(a + "ed"), x.removeClass(o), y = null, x.eq(b).addClass(s).siblings().removeClass(s), e + t > h.height() && e >= t && i.addClass(a + "up"), T()
                            }, w = function (e) {
                                i.removeClass(a + "ed " + a + "up"), k.blur(), y = null, e || $(k.val(), function (e) {
                                    var i = p[0].selectedIndex;
                                    e && (d = t(p[0].options[i]).html(), 0 === i && d === k.attr("placeholder") && (d = ""), k.val(d || ""))
                                })
                            }, T = function () {
                                var e = g.children("dd." + s);
                                if (e[0]) {
                                    var t = e.position().top, i = g.height(), a = e.height();
                                    t > i && g.scrollTop(t + g.scrollTop() - i + a - 5), t < 0 && g.scrollTop(t + g.scrollTop() - 5)
                                }
                            };
                            m.on("click", function (e) {
                                i.hasClass(a + "ed") ? w() : (v(e, !0), C()), g.find("." + r).remove()
                            }), m.find(".layui-edge").on("click", function () {
                                k.focus()
                            }), k.on("keyup", function (e) {
                                var t = e.keyCode;
                                9 === t && C()
                            }).on("keydown", function (e) {
                                var t = e.keyCode;
                                9 === t && w();
                                var i = function (t, a) {
                                    var n, l;
                                    e.preventDefault();
                                    var r = function () {
                                        var e = g.children("dd." + s);
                                        if (g.children("dd." + o)[0] && "next" === t) {
                                            var i = g.children("dd:not(." + o + ",." + c + ")"), n = i.eq(0).index();
                                            if (n >= 0 && n < e.index() && !i.hasClass(s)) return i.eq(0).prev()[0] ? i.eq(0).prev() : g.children(":last")
                                        }
                                        return a && a[0] ? a : y && y[0] ? y : e
                                    }();
                                    return l = r[t](), n = r[t]("dd:not(." + o + ")"), l[0] ? (y = r[t](), n[0] && !n.hasClass(c) || !y[0] ? (n.addClass(s).siblings().removeClass(s), void T()) : i(t, y)) : y = null
                                };
                                38 === t && i("prev"), 40 === t && i("next"), 13 === t && (e.preventDefault(), g.children("dd." + s).trigger("click"))
                            });
                            var $ = function (e, i, a) {
                                var n = 0;
                                layui.each(x, function () {
                                    var i = t(this), l = i.text(), r = l.indexOf(e) === -1;
                                    ("" === e || "blur" === a ? e !== l : r) && n++, "keyup" === a && i[r ? "addClass" : "removeClass"](o)
                                });
                                var l = n === x.length;
                                return i(l), l
                            }, q = function (e) {
                                var t = this.value, i = e.keyCode;
                                return 9 !== i && 13 !== i && 37 !== i && 38 !== i && 39 !== i && 40 !== i && ($(t, function (e) {
                                    e ? g.find("." + r)[0] || g.append('<p class="' + r + '">无匹配项</p>') : g.find("." + r).remove()
                                }, "keyup"), "" === t && g.find("." + r).remove(), void T())
                            };
                            f && k.on("keyup", q).on("blur", function (i) {
                                var a = p[0].selectedIndex;
                                e = k, d = t(p[0].options[a]).html(), 0 === a && d === k.attr("placeholder") && (d = ""), setTimeout(function () {
                                    $(k.val(), function (e) {
                                        d || k.val("")
                                    }, "blur")
                                }, 200)
                            }), x.on("click", function () {
                                var e = t(this), a = e.attr("lay-value"), n = p.attr("lay-filter");
                                return !e.hasClass(c) && (e.hasClass("layui-select-tips") ? k.val("") : (k.val(e.text()), e.addClass(s)), e.siblings().removeClass(s), p.val(a).removeClass("layui-form-danger"), layui.event.call(this, l, "select(" + n + ")", {
                                    elem: p[0],
                                    value: a,
                                    othis: i
                                }), w(!0), !1)
                            }), i.find("dl>dt").on("click", function (e) {
                                return !1
                            }), t(document).off("click", v).on("click", v)
                        }
                    };
                f.each(function (e, l) {
                    var r = t(this), o = r.next("." + a), u = this.disabled, d = l.value,
                        f = t(l.options[l.selectedIndex]), v = l.options[0];
                    if ("string" == typeof r.attr("lay-ignore")) return r.show();
                    var h = "string" == typeof r.attr("lay-search"), p = v ? v.value ? i : v.innerHTML || i : i,
                        m = t(['<div class="' + (h ? "" : "layui-unselect ") + a, (u ? " layui-select-disabled" : "") + '">', '<div class="' + n + '">', '<input type="text" placeholder="' + p + '" ' + ('value="' + (d ? f.html() : "") + '"') + (!u && h ? "" : " readonly") + ' class="layui-input' + (h ? "" : " layui-unselect") + (u ? " " + c : "") + '">', '<i class="layui-edge"></i></div>', '<dl class="layui-anim layui-anim-upbit' + (r.find("optgroup")[0] ? " layui-select-group" : "") + '">', function (e) {
                            var t = [];
                            return layui.each(e, function (e, a) {
                                0 !== e || a.value ? "optgroup" === a.tagName.toLowerCase() ? t.push("<dt>" + a.label + "</dt>") : t.push('<dd lay-value="' + a.value + '" class="' + (d === a.value ? s : "") + (a.disabled ? " " + c : "") + '">' + a.innerHTML + "</dd>") : t.push('<dd lay-value="" class="layui-select-tips">' + (a.innerHTML || i) + "</dd>")
                            }), 0 === t.length && t.push('<dd lay-value="" class="' + c + '">没有选项</dd>'), t.join("")
                        }(r.find("*")) + "</dl>", "</div>"].join(""));
                    o[0] && o.remove(), r.after(m), y.call(this, m, u, h)
                })
            }, checkbox: function () {
                var e = {
                    checkbox: ["layui-form-checkbox", "layui-form-checked", "checkbox"],
                    _switch: ["layui-form-switch", "layui-form-onswitch", "switch"]
                }, i = u.find("input[type=checkbox]"), a = function (e, i) {
                    var a = t(this);
                    e.on("click", function () {
                        var t = a.attr("lay-filter"), n = (a.attr("lay-text") || "").split("|");
                        a[0].disabled || (a[0].checked ? (a[0].checked = !1, e.removeClass(i[1]).find("em").text(n[1])) : (a[0].checked = !0, e.addClass(i[1]).find("em").text(n[0])), layui.event.call(a[0], l, i[2] + "(" + t + ")", {
                            elem: a[0],
                            value: a[0].value,
                            othis: e
                        }))
                    })
                };
                i.each(function (i, n) {
                    var l = t(this), r = l.attr("lay-skin"), s = (l.attr("lay-text") || "").split("|"),
                        o = this.disabled;
                    "switch" === r && (r = "_" + r);
                    var u = e[r] || e.checkbox;
                    if ("string" == typeof l.attr("lay-ignore")) return l.show();
                    var d = l.next("." + u[0]),
                        f = t(['<div class="layui-unselect ' + u[0], n.checked ? " " + u[1] : "", o ? " layui-checkbox-disbaled " + c : "", '"', r ? ' lay-skin="' + r + '"' : "", ">", function () {
                            var e = n.title.replace(/\s/g, ""), t = {
                                checkbox: [e ? "<span>" + n.title + "</span>" : "", '<i class="layui-icon layui-icon-ok"></i>'].join(""),
                                _switch: "<em>" + ((n.checked ? s[0] : s[1]) || "") + "</em><i></i>"
                            };
                            return t[r] || t.checkbox
                        }(), "</div>"].join(""));
                    d[0] && d.remove(), l.after(f), a.call(this, f, u)
                })
            }, radio: function () {
                var e = "layui-form-radio", i = ["&#xe643;", "&#xe63f;"], a = u.find("input[type=radio]"),
                    n = function (a) {
                        var n = t(this), s = "layui-anim-scaleSpring";
                        a.on("click", function () {
                            var o = n[0].name, c = n.parents(r), u = n.attr("lay-filter"),
                                d = c.find("input[name=" + o.replace(/(\.|#|\[|\])/g, "\\$1") + "]");
                            n[0].disabled || (layui.each(d, function () {
                                var a = t(this).next("." + e);
                                this.checked = !1, a.removeClass(e + "ed"), a.find(".layui-icon").removeClass(s).html(i[1])
                            }), n[0].checked = !0, a.addClass(e + "ed"), a.find(".layui-icon").addClass(s).html(i[0]), layui.event.call(n[0], l, "radio(" + u + ")", {
                                elem: n[0],
                                value: n[0].value,
                                othis: a
                            }))
                        })
                    };
                a.each(function (a, l) {
                    var r = t(this), s = r.next("." + e), o = this.disabled;
                    if ("string" == typeof r.attr("lay-ignore")) return r.show();
                    s[0] && s.remove();
                    var u = t(['<div class="layui-unselect ' + e, l.checked ? " " + e + "ed" : "", (o ? " layui-radio-disbaled " + c : "") + '">', '<i class="layui-anim layui-icon">' + i[l.checked ? 0 : 1] + "</i>", "<div>" + function () {
                        var e = l.title || "";
                        return "string" == typeof r.next().attr("lay-radio") && (e = r.next().html(), r.next().remove()), e
                    }() + "</div>", "</div>"].join(""));
                    r.after(u), n.call(this, u)
                })
            }
        };
        return e ? d[e] ? d[e]() : a.error("不支持的" + e + "表单渲染") : layui.each(d, function (e, t) {
            t()
        }), n
    };
    var d = function () {
        var e = null, a = f.config.verify, s = "layui-form-danger", o = {}, c = t(this), u = c.parents(r),
            d = u.find("*[lay-verify]"), v = c.parents("form")[0], h = c.attr("lay-filter");
        return layui.each(d, function (l, r) {
            var o = t(this), c = o.attr("lay-verify").split("|"), u = o.attr("lay-verType"), d = o.val();
            if (o.removeClass(s), layui.each(c, function (t, l) {
                var c, f = "", v = "function" == typeof a[l];
                if (a[l]) {
                    var c = v ? f = a[l](d, r) : !a[l][0].test(d);
                    if (f = f || a[l][1], "required" === l && (f = o.attr("lay-reqText") || f), c) return "tips" === u ? i.tips(f, function () {
                        return "string" == typeof o.attr("lay-ignore") || "select" !== r.tagName.toLowerCase() && !/^checkbox|radio$/.test(r.type) ? o : o.next()
                    }(), {tips: 1}) : "alert" === u ? i.alert(f, {
                        title: "提示",
                        shadeClose: !0
                    }) : /\bstring|number\b/.test(typeof f) && i.msg(f, {
                        icon: 5,
                        shift: 6
                    }), n.android || n.ios || setTimeout(function () {
                        r.focus()
                    }, 7), o.addClass(s), e = !0
                }
            }), e) return e
        }), !e && (o = f.getValue(null, u), layui.event.call(this, l, "submit(" + h + ")", {
            elem: this,
            form: v,
            field: o
        }))
    }, f = new u, v = t(document), h = t(window);
    t(function () {
        f.render()
    }), v.on("reset", r, function () {
        var e = t(this).attr("lay-filter");
        setTimeout(function () {
            f.render(null, e)
        }, 50)
    }), v.on("submit", r, d).on("click", "*[lay-submit]", d), e(l, f)
});
layui.define("form", function (e) {
    "use strict";
    var i = layui.$, a = layui.form, n = layui.layer, t = "tree", r = {
            config: {}, index: layui[t] ? layui[t].index + 1e4 : 0, set: function (e) {
                var a = this;
                return a.config = i.extend({}, a.config, e), a
            }, on: function (e, i) {
                return layui.onevent.call(this, t, e, i)
            }
        }, l = function () {
            var e = this, i = e.config, a = i.id || e.index;
            return l.that[a] = e, l.config[a] = i, {
                config: i, reload: function (i) {
                    e.reload.call(e, i)
                }, getChecked: function () {
                    return e.getChecked.call(e)
                }, setChecked: function (i) {
                    return e.setChecked.call(e, i)
                }
            }
        }, c = "layui-hide", d = "layui-disabled", s = "layui-tree-set", o = "layui-tree-iconClick",
        h = "layui-icon-addition", u = "layui-icon-subtraction", p = "layui-tree-entry", f = "layui-tree-main",
        y = "layui-tree-txt", v = "layui-tree-pack", C = "layui-tree-spread", k = "layui-tree-setLineShort",
        m = "layui-tree-showLine", x = "layui-tree-lineExtend", b = function (e) {
            var a = this;
            a.index = ++r.index, a.config = i.extend({}, a.config, r.config, e), a.render()
        };
    b.prototype.config = {
        data: [],
        showCheckbox: !1,
        showLine: !0,
        accordion: !1,
        onlyIconControl: !1,
        isJump: !1,
        edit: !1,
        text: {defaultNodeName: "未命名", none: "无数据"}
    }, b.prototype.reload = function (e) {
        var a = this;
        layui.each(e, function (e, i) {
            i.constructor === Array && delete a.config[e]
        }), a.config = i.extend(!0, {}, a.config, e), a.render()
    }, b.prototype.render = function () {
        var e = this, a = e.config;
        e.checkids = [];
        var n = i('<div class="layui-tree' + (a.showCheckbox ? " layui-form" : "") + (a.showLine ? " layui-tree-line" : "") + '" lay-filter="LAY-tree-' + e.index + '"></div>');
        e.tree(n);
        var t = a.elem = i(a.elem);
        if (t[0]) {
            if (e.key = a.id || e.index, e.elem = n, e.elemNone = i('<div class="layui-tree-emptyText">' + a.text.none + "</div>"), t.html(e.elem), 0 == e.elem.find(".layui-tree-set").length) return e.elem.append(e.elemNone);
            a.showCheckbox && e.renderForm("checkbox"), e.elem.find(".layui-tree-set").each(function () {
                var e = i(this);
                e.parent(".layui-tree-pack")[0] || e.addClass("layui-tree-setHide"), !e.next()[0] && e.parents(".layui-tree-pack").eq(1).hasClass("layui-tree-lineExtend") && e.addClass(k), e.next()[0] || e.parents(".layui-tree-set").eq(0).next()[0] || e.addClass(k)
            }), e.events()
        }
    }, b.prototype.renderForm = function (e) {
        a.render(e, "LAY-tree-" + this.index)
    }, b.prototype.tree = function (e, a) {
        var n = this, t = n.config, r = a || t.data;
        layui.each(r, function (a, r) {
            var l = r.children && r.children.length > 0,
                o = i('<div class="layui-tree-pack" ' + (r.spread ? 'style="display: block;"' : "") + "></div>"),
                h = i(['<div data-id="' + r.id + '" class="layui-tree-set' + (r.spread ? " layui-tree-spread" : "") + (r.checked ? " layui-tree-checkedFirst" : "") + '">', '<div class="layui-tree-entry">', '<div class="layui-tree-main">', function () {
                    return t.showLine ? l ? '<span class="layui-tree-iconClick layui-tree-icon"><i class="layui-icon ' + (r.spread ? "layui-icon-subtraction" : "layui-icon-addition") + '"></i></span>' : '<span class="layui-tree-iconClick"><i class="layui-icon layui-icon-file"></i></span>' : '<span class="layui-tree-iconClick"><i class="layui-tree-iconArrow ' + (l ? "" : c) + '"></i></span>'
                }(), function () {
                    return t.showCheckbox ? '<input type="checkbox" name="' + (r.field || "layuiTreeCheck_" + r.id) + '" same="layuiTreeCheck" lay-skin="primary" ' + (r.disabled ? "disabled" : "") + ' value="' + r.id + '">' : ""
                }(), function () {
                    return t.isJump && r.href ? '<a href="' + r.href + '" target="_blank" class="' + y + '">' + (r.title || r.label || t.text.defaultNodeName) + "</a>" : '<span class="' + y + (r.disabled ? " " + d : "") + '">' + (r.title || r.label || t.text.defaultNodeName) + "</span>"
                }(), "</div>", function () {
                    if (!t.edit) return "";
                    var e = {
                        add: '<i class="layui-icon layui-icon-add-1"  data-type="add"></i>',
                        update: '<i class="layui-icon layui-icon-edit" data-type="update"></i>',
                        del: '<i class="layui-icon layui-icon-delete" data-type="del"></i>'
                    }, i = ['<div class="layui-btn-group layui-tree-btnGroup">'];
                    return t.edit === !0 && (t.edit = ["update", "del"]), "object" == typeof t.edit ? (layui.each(t.edit, function (a, n) {
                        i.push(e[n] || "")
                    }), i.join("") + "</div>") : void 0
                }(), "</div></div>"].join(""));
            l && (h.append(o), n.tree(o, r.children)), e.append(h), h.prev("." + s)[0] && h.prev().children(".layui-tree-pack").addClass("layui-tree-showLine"), l || h.parent(".layui-tree-pack").addClass("layui-tree-lineExtend"), n.spread(h, r), t.showCheckbox && (r.checked && n.checkids.push(r.id), n.checkClick(h, r)), t.edit && n.operate(h, r)
        })
    }, b.prototype.spread = function (e, a) {
        var n = this, t = n.config, r = e.children("." + p), l = r.children("." + f), c = r.find("." + o),
            k = r.find("." + y), m = t.onlyIconControl ? c : l, x = "";
        m.on("click", function (i) {
            var a = e.children("." + v),
                n = m.children(".layui-icon")[0] ? m.children(".layui-icon") : m.find(".layui-tree-icon").children(".layui-icon");
            if (a[0]) {
                if (e.hasClass(C)) e.removeClass(C), a.slideUp(200), n.removeClass(u).addClass(h); else if (e.addClass(C), a.slideDown(200), n.addClass(u).removeClass(h), t.accordion) {
                    var r = e.siblings("." + s);
                    r.removeClass(C), r.children("." + v).slideUp(200), r.find(".layui-tree-icon").children(".layui-icon").removeClass(u).addClass(h)
                }
            } else x = "normal"
        }), k.on("click", function () {
            var n = i(this);
            n.hasClass(d) || (x = e.hasClass(C) ? t.onlyIconControl ? "open" : "close" : t.onlyIconControl ? "close" : "open", t.click && t.click({
                elem: e,
                state: x,
                data: a
            }))
        })
    }, b.prototype.setCheckbox = function (e, i, a) {
        var n = this, t = (n.config, a.prop("checked"));
        if (!a.prop("disabled")) {
            if ("object" == typeof i.children || e.find("." + v)[0]) {
                var r = e.find("." + v).find('input[same="layuiTreeCheck"]');
                r.each(function () {
                    this.disabled || (this.checked = t)
                })
            }
            var l = function (e) {
                if (e.parents("." + s)[0]) {
                    var i, a = e.parent("." + v), n = a.parent(), r = a.prev().find('input[same="layuiTreeCheck"]');
                    t ? r.prop("checked", t) : (a.find('input[same="layuiTreeCheck"]').each(function () {
                        this.checked && (i = !0)
                    }), i || r.prop("checked", !1)), l(n)
                }
            };
            l(e), n.renderForm("checkbox")
        }
    }, b.prototype.checkClick = function (e, a) {
        var n = this, t = n.config, r = e.children("." + p), l = r.children("." + f);
        l.on("click", 'input[same="layuiTreeCheck"]+', function (r) {
            layui.stope(r);
            var l = i(this).prev(), c = l.prop("checked");
            l.prop("disabled") || (n.setCheckbox(e, a, l), t.oncheck && t.oncheck({elem: e, checked: c, data: a}))
        })
    }, b.prototype.operate = function (e, a) {
        var t = this, r = t.config, l = e.children("." + p), d = l.children("." + f);
        l.children(".layui-tree-btnGroup").on("click", ".layui-icon", function (l) {
            layui.stope(l);
            var f = i(this).data("type"), b = e.children("." + v), g = {data: a, type: f, elem: e};
            if ("add" == f) {
                b[0] || (r.showLine ? (d.find("." + o).addClass("layui-tree-icon"), d.find("." + o).children(".layui-icon").addClass(h).removeClass("layui-icon-file")) : d.find(".layui-tree-iconArrow").removeClass(c), e.append('<div class="layui-tree-pack"></div>'));
                var w = r.operate && r.operate(g), N = {};
                if (N.title = r.text.defaultNodeName, N.id = w, t.tree(e.children("." + v), [N]), r.showLine) if (b[0]) b.hasClass(x) || b.addClass(x), e.find("." + v).each(function () {
                    i(this).children("." + s).last().addClass(k)
                }), b.children("." + s).last().prev().hasClass(k) ? b.children("." + s).last().prev().removeClass(k) : b.children("." + s).last().removeClass(k), !e.parent("." + v)[0] && e.next()[0] && b.children("." + s).last().removeClass(k); else {
                    var T = e.siblings("." + s), L = 1, A = e.parent("." + v);
                    layui.each(T, function (e, a) {
                        i(a).children("." + v)[0] || (L = 0)
                    }), 1 == L ? (T.children("." + v).addClass(m), T.children("." + v).children("." + s).removeClass(k), e.children("." + v).addClass(m), A.removeClass(x), A.children("." + s).last().children("." + v).children("." + s).last().addClass(k)) : e.children("." + v).children("." + s).addClass(k)
                }
                if (!r.showCheckbox) return;
                if (d.find('input[same="layuiTreeCheck"]')[0].checked) {
                    var I = e.children("." + v).children("." + s).last();
                    I.find('input[same="layuiTreeCheck"]')[0].checked = !0
                }
                t.renderForm("checkbox")
            } else if ("update" == f) {
                var F = d.children("." + y).html();
                d.children("." + y).html(""), d.append('<input type="text" class="layui-tree-editInput">'), d.children(".layui-tree-editInput").val(F).focus();
                var j = function (e) {
                    var i = e.val().trim();
                    i = i ? i : r.text.defaultNodeName, e.remove(), d.children("." + y).html(i), g.data.title = i, r.operate && r.operate(g)
                };
                d.children(".layui-tree-editInput").blur(function () {
                    j(i(this))
                }), d.children(".layui-tree-editInput").on("keydown", function (e) {
                    13 === e.keyCode && (e.preventDefault(), j(i(this)))
                })
            } else n.confirm('确认删除该节点 "<span style="color: #999;">' + (a.title || "") + '</span>" 吗？', function (a) {
                if (r.operate && r.operate(g), g.status = "remove", n.close(a), !e.prev("." + s)[0] && !e.next("." + s)[0] && !e.parent("." + v)[0]) return e.remove(), void t.elem.append(t.elemNone);
                if (e.siblings("." + s).children("." + p)[0]) {
                    if (r.showCheckbox) {
                        var l = function (e) {
                            if (e.parents("." + s)[0]) {
                                var a = e.siblings("." + s).children("." + p), n = e.parent("." + v).prev(),
                                    r = n.find('input[same="layuiTreeCheck"]')[0], c = 1, d = 0;
                                0 == r.checked && (a.each(function (e, a) {
                                    var n = i(a).find('input[same="layuiTreeCheck"]')[0];
                                    0 != n.checked || n.disabled || (c = 0), n.disabled || (d = 1)
                                }), 1 == c && 1 == d && (r.checked = !0, t.renderForm("checkbox"), l(n.parent("." + s))))
                            }
                        };
                        l(e)
                    }
                    if (r.showLine) {
                        var d = e.siblings("." + s), h = 1, f = e.parent("." + v);
                        layui.each(d, function (e, a) {
                            i(a).children("." + v)[0] || (h = 0)
                        }), 1 == h ? (b[0] || (f.removeClass(x), d.children("." + v).addClass(m), d.children("." + v).children("." + s).removeClass(k)), e.next()[0] ? f.children("." + s).last().children("." + v).children("." + s).last().addClass(k) : e.prev().children("." + v).children("." + s).last().addClass(k), e.next()[0] || e.parents("." + s)[1] || e.parents("." + s).eq(0).next()[0] || e.prev("." + s).addClass(k)) : !e.next()[0] && e.hasClass(k) && e.prev().addClass(k)
                    }
                } else {
                    var y = e.parent("." + v).prev();
                    if (r.showLine) {
                        y.find("." + o).removeClass("layui-tree-icon"), y.find("." + o).children(".layui-icon").removeClass(u).addClass("layui-icon-file");
                        var w = y.parents("." + v).eq(0);
                        w.addClass(x), w.children("." + s).each(function () {
                            i(this).children("." + v).children("." + s).last().addClass(k)
                        })
                    } else y.find(".layui-tree-iconArrow").addClass(c);
                    e.parents("." + s).eq(0).removeClass(C), e.parent("." + v).remove()
                }
                e.remove()
            })
        })
    }, b.prototype.events = function () {
        var e = this, a = e.config;
        e.elem.find(".layui-tree-checkedFirst");
        e.setChecked(e.checkids), e.elem.find(".layui-tree-search").on("keyup", function () {
            var n = i(this), t = n.val(), r = n.nextAll(), l = [];
            r.find("." + y).each(function () {
                var e = i(this).parents("." + p);
                if (i(this).html().indexOf(t) != -1) {
                    l.push(i(this).parent());
                    var a = function (e) {
                        e.addClass("layui-tree-searchShow"), e.parent("." + v)[0] && a(e.parent("." + v).parent("." + s))
                    };
                    a(e.parent("." + s))
                }
            }), r.find("." + p).each(function () {
                var e = i(this).parent("." + s);
                e.hasClass("layui-tree-searchShow") || e.addClass(c)
            }), 0 == r.find(".layui-tree-searchShow").length && e.elem.append(e.elemNone), a.onsearch && a.onsearch({elem: l})
        }), e.elem.find(".layui-tree-search").on("keydown", function () {
            i(this).nextAll().find("." + p).each(function () {
                var e = i(this).parent("." + s);
                e.removeClass("layui-tree-searchShow " + c)
            }), i(".layui-tree-emptyText")[0] && i(".layui-tree-emptyText").remove()
        })
    }, b.prototype.getChecked = function () {
        var e = this, a = e.config, n = [], t = [];
        e.elem.find(".layui-form-checked").each(function () {
            n.push(i(this).prev()[0].value)
        });
        var r = function (e, a) {
            layui.each(e, function (e, t) {
                layui.each(n, function (e, n) {
                    if (t.id == n) {
                        var l = i.extend({}, t);
                        return delete l.children, a.push(l), t.children && (l.children = [], r(t.children, l.children)), !0
                    }
                })
            })
        };
        return r(i.extend({}, a.data), t), t
    }, b.prototype.setChecked = function (e) {
        var a = this;
        a.config;
        a.elem.find("." + s).each(function (a, n) {
            var t = i(this).data("id"), r = i(n).children("." + p).find('input[same="layuiTreeCheck"]'), l = r.next();
            if ("number" == typeof e) {
                if (t == e) return r[0].checked || l.click(), !1
            } else "object" == typeof e && layui.each(e, function (e, i) {
                if (i == t && !r[0].checked) return l.click(), !0
            })
        })
    }, l.that = {}, l.config = {}, r.reload = function (e, i) {
        var a = l.that[e];
        return a.reload(i), l.call(a)
    }, r.getChecked = function (e) {
        var i = l.that[e];
        return i.getChecked()
    }, r.setChecked = function (e, i) {
        var a = l.that[e];
        return a.setChecked(i)
    }, r.render = function (e) {
        var i = new b(e);
        return l.call(i)
    }, e(t, r)
});
layui.define(["laytpl", "form"], function (e) {
    "use strict";
    var a = layui.$, t = layui.laytpl, i = layui.form, n = "transfer", l = {
            config: {}, index: layui[n] ? layui[n].index + 1e4 : 0, set: function (e) {
                var t = this;
                return t.config = a.extend({}, t.config, e), t
            }, on: function (e, a) {
                return layui.onevent.call(this, n, e, a)
            }
        }, r = function () {
            var e = this, a = e.config, t = a.id || e.index;
            return r.that[t] = e, r.config[t] = a, {
                config: a, reload: function (a) {
                    e.reload.call(e, a)
                }, getData: function () {
                    return e.getData.call(e)
                }
            }
        }, c = "layui-hide", o = "layui-btn-disabled", d = "layui-none", s = "layui-transfer-box",
        u = "layui-transfer-header", h = "layui-transfer-search", f = "layui-transfer-active",
        y = "layui-transfer-data", p = function (e) {
            return e = e || {}, ['<div class="layui-transfer-box" data-index="' + e.index + '">', '<div class="layui-transfer-header">', '<input type="checkbox" name="' + e.checkAllName + '" lay-filter="layTransferCheckbox" lay-type="all" lay-skin="primary" title="{{ d.data.title[' + e.index + "] || 'list" + (e.index + 1) + "' }}\">", "</div>", "{{# if(d.data.showSearch){ }}", '<div class="layui-transfer-search">', '<i class="layui-icon layui-icon-search"></i>', '<input type="input" class="layui-input" placeholder="关键词搜索">', "</div>", "{{# } }}", '<ul class="layui-transfer-data"></ul>', "</div>"].join("")
        }, v = ['<div class="layui-transfer layui-form layui-border-box" lay-filter="LAY-transfer-{{ d.index }}">', p({
            index: 0,
            checkAllName: "layTransferLeftCheckAll"
        }), '<div class="layui-transfer-active">', '<button type="button" class="layui-btn layui-btn-sm layui-btn-primary layui-btn-disabled" data-index="0">', '<i class="layui-icon layui-icon-next"></i>', "</button>", '<button type="button" class="layui-btn layui-btn-sm layui-btn-primary layui-btn-disabled" data-index="1">', '<i class="layui-icon layui-icon-prev"></i>', "</button>", "</div>", p({
            index: 1,
            checkAllName: "layTransferRightCheckAll"
        }), "</div>"].join(""), x = function (e) {
            var t = this;
            t.index = ++l.index, t.config = a.extend({}, t.config, l.config, e), t.render()
        };
    x.prototype.config = {
        title: ["列表一", "列表二"],
        width: 200,
        height: 360,
        data: [],
        value: [],
        showSearch: !1,
        id: "",
        text: {none: "无数据", searchNone: "无匹配数据"}
    }, x.prototype.reload = function (e) {
        var t = this;
        t.config = a.extend({}, t.config, e), t.render()
    }, x.prototype.render = function () {
        var e = this, i = e.config, n = e.elem = a(t(v).render({data: i, index: e.index})), l = i.elem = a(i.elem);
        l[0] && (i.data = i.data || [], i.value = i.value || [], e.key = i.id || e.index, l.html(e.elem), e.layBox = e.elem.find("." + s), e.layHeader = e.elem.find("." + u), e.laySearch = e.elem.find("." + h), e.layData = n.find("." + y), e.layBtn = n.find("." + f + " .layui-btn"), e.layBox.css({
            width: i.width,
            height: i.height
        }), e.layData.css({
            height: function () {
                return i.height - e.layHeader.outerHeight() - e.laySearch.outerHeight() - 2
            }()
        }), e.renderData(), e.events())
    }, x.prototype.renderData = function () {
        var e = this, a = (e.config, [{checkName: "layTransferLeftCheck", views: []}, {
            checkName: "layTransferRightCheck",
            views: []
        }]);
        e.parseData(function (e) {
            var t = e.selected ? 1 : 0,
                i = ["<li>", '<input type="checkbox" name="' + a[t].checkName + '" lay-skin="primary" lay-filter="layTransferCheckbox" title="' + e.title + '"' + (e.disabled ? " disabled" : "") + (e.checked ? " checked" : "") + ' value="' + e.value + '">', "</li>"].join("");
            a[t].views.push(i), delete e.selected
        }), e.layData.eq(0).html(a[0].views.join("")), e.layData.eq(1).html(a[1].views.join("")), e.renderCheckBtn()
    }, x.prototype.renderForm = function (e) {
        i.render(e, "LAY-transfer-" + this.index)
    }, x.prototype.renderCheckBtn = function (e) {
        var t = this, i = t.config;
        e = e || {}, t.layBox.each(function (n) {
            var l = a(this), r = l.find("." + y), d = l.find("." + u).find('input[type="checkbox"]'),
                s = r.find('input[type="checkbox"]'), h = 0, f = !1;
            if (s.each(function () {
                var e = a(this).data("hide");
                (this.checked || this.disabled || e) && h++, this.checked && !e && (f = !0)
            }), d.prop("checked", f && h === s.length), t.layBtn.eq(n)[f ? "removeClass" : "addClass"](o), !e.stopNone) {
                var p = r.children("li:not(." + c + ")").length;
                t.noneView(r, p ? "" : i.text.none)
            }
        }), t.renderForm("checkbox")
    }, x.prototype.noneView = function (e, t) {
        var i = a('<p class="layui-none">' + (t || "") + "</p>");
        e.find("." + d)[0] && e.find("." + d).remove(), t.replace(/\s/g, "") && e.append(i)
    }, x.prototype.setValue = function () {
        var e = this, t = e.config, i = [];
        return e.layBox.eq(1).find("." + y + ' input[type="checkbox"]').each(function () {
            var e = a(this).data("hide");
            e || i.push(this.value)
        }), t.value = i, e
    }, x.prototype.parseData = function (e) {
        var t = this, i = t.config, n = [];
        return layui.each(i.data, function (t, l) {
            l = ("function" == typeof i.parseData ? i.parseData(l) : l) || l, n.push(l = a.extend({}, l)), layui.each(i.value, function (e, a) {
                a == l.value && (l.selected = !0)
            }), e && e(l)
        }), i.data = n, t
    }, x.prototype.getData = function (e) {
        var a = this, t = a.config, i = [];
        return a.setValue(), layui.each(e || t.value, function (e, a) {
            layui.each(t.data, function (e, t) {
                delete t.selected, a == t.value && i.push(t)
            })
        }), i
    }, x.prototype.events = function () {
        var e = this, t = e.config;
        e.elem.on("click", 'input[lay-filter="layTransferCheckbox"]+', function () {
            var t = a(this).prev(), i = t[0].checked, n = t.parents("." + s).eq(0).find("." + y);
            t[0].disabled || ("all" === t.attr("lay-type") && n.find('input[type="checkbox"]').each(function () {
                this.disabled || (this.checked = i)
            }), e.renderCheckBtn({stopNone: !0}))
        }), e.layBtn.on("click", function () {
            var i = a(this), n = i.data("index"), l = e.layBox.eq(n), r = [];
            if (!i.hasClass(o)) {
                e.layBox.eq(n).each(function (t) {
                    var i = a(this), n = i.find("." + y);
                    n.children("li").each(function () {
                        var t = a(this), i = t.find('input[type="checkbox"]'), n = i.data("hide");
                        i[0].checked && !n && (i[0].checked = !1, l.siblings("." + s).find("." + y).append(t.clone()), t.remove(), r.push(i[0].value)), e.setValue()
                    })
                }), e.renderCheckBtn();
                var c = l.siblings("." + s).find("." + h + " input");
                "" === c.val() || c.trigger("keyup"), t.onchange && t.onchange(e.getData(r), n)
            }
        }), e.laySearch.find("input").on("keyup", function () {
            var i = this.value, n = a(this).parents("." + h).eq(0).siblings("." + y), l = n.children("li");
            l.each(function () {
                var e = a(this), t = e.find('input[type="checkbox"]'), n = t[0].title.indexOf(i) !== -1;
                e[n ? "removeClass" : "addClass"](c), t.data("hide", !n)
            }), e.renderCheckBtn();
            var r = l.length === n.children("li." + c).length;
            e.noneView(n, r ? t.text.searchNone : "")
        })
    }, r.that = {}, r.config = {}, l.reload = function (e, a) {
        var t = r.that[e];
        return t.reload(a), r.call(t)
    }, l.getData = function (e) {
        var a = r.that[e];
        return a.getData()
    }, l.render = function (e) {
        var a = new x(e);
        return r.call(a)
    }, e(n, l)
});
layui.define(["laytpl", "laypage", "layer", "form", "util"], function (e) {
    "use strict";
    var t = layui.$, i = layui.laytpl, a = layui.laypage, l = layui.layer, n = layui.form,
        o = (layui.util, layui.hint()), r = layui.device(), d = {
            config: {checkName: "LAY_CHECKED", indexName: "LAY_TABLE_INDEX"},
            cache: {},
            index: layui.table ? layui.table.index + 1e4 : 0,
            set: function (e) {
                var i = this;
                return i.config = t.extend({}, i.config, e), i
            },
            on: function (e, t) {
                return layui.onevent.call(this, y, e, t)
            }
        }, c = function () {
            var e = this, t = e.config, i = t.id || t.index;
            return i && (c.that[i] = e, c.config[i] = t), {
                config: t, reload: function (t, i) {
                    e.reload.call(e, t, i)
                }, setColsWidth: function () {
                    e.setColsWidth.call(e)
                }, resize: function () {
                    e.resize.call(e)
                }
            }
        }, s = function (e) {
            var t = c.config[e];
            return t || o.error(e ? "The table instance with ID '" + e + "' not found" : "ID argument required"), t || null
        }, u = function (e, a, l, n) {
            var o = e.templet ? function () {
                return "function" == typeof e.templet ? e.templet(l) : i(t(e.templet).html() || String(a)).render(l)
            }() : a;
            return n ? t("<div>" + o + "</div>").text() : o
        }, y = "table", h = ".layui-table", f = "layui-hide", p = "layui-none", v = "layui-table-view",
        m = ".layui-table-tool", g = ".layui-table-box", b = ".layui-table-init", x = ".layui-table-header",
        k = ".layui-table-body", C = ".layui-table-main", w = ".layui-table-fixed", T = ".layui-table-fixed-l",
        N = ".layui-table-fixed-r", A = ".layui-table-total", L = ".layui-table-page", S = ".layui-table-sort",
        R = "layui-table-edit", W = "layui-table-hover", _ = function (e) {
            var t = '{{#if(item2.colspan){}} colspan="{{item2.colspan}}"{{#} if(item2.rowspan){}} rowspan="{{item2.rowspan}}"{{#}}}';
            return e = e || {}, ['<table cellspacing="0" cellpadding="0" border="0" class="layui-table" ', '{{# if(d.data.skin){ }}lay-skin="{{d.data.skin}}"{{# } }} {{# if(d.data.size){ }}lay-size="{{d.data.size}}"{{# } }} {{# if(d.data.even){ }}lay-even{{# } }}>', "<thead>", "{{# layui.each(d.data.cols, function(i1, item1){ }}", "<tr>", "{{# layui.each(item1, function(i2, item2){ }}", '{{# if(item2.fixed && item2.fixed !== "right"){ left = true; } }}', '{{# if(item2.fixed === "right"){ right = true; } }}', function () {
                return e.fixed && "right" !== e.fixed ? '{{# if(item2.fixed && item2.fixed !== "right"){ }}' : "right" === e.fixed ? '{{# if(item2.fixed === "right"){ }}' : ""
            }(), "{{# var isSort = !(item2.colGroup) && item2.sort; }}", '<th data-field="{{ item2.field||i2 }}" data-key="{{d.index}}-{{i1}}-{{i2}}" {{# if( item2.parentKey){ }}data-parentkey="{{ item2.parentKey }}"{{# } }} {{# if(item2.minWidth){ }}data-minwidth="{{item2.minWidth}}"{{# } }} ' + t + ' {{# if(item2.unresize || item2.colGroup){ }}data-unresize="true"{{# } }} class="{{# if(item2.hide){ }}layui-hide{{# } }}{{# if(isSort){ }} layui-unselect{{# } }}{{# if(!item2.field){ }} layui-table-col-special{{# } }}">', '<div class="layui-table-cell laytable-cell-', "{{# if(item2.colGroup){ }}", "group", "{{# } else { }}", "{{d.index}}-{{i1}}-{{i2}}", '{{# if(item2.type !== "normal"){ }}', " laytable-cell-{{ item2.type }}", "{{# } }}", "{{# } }}", '" {{#if(item2.align){}}align="{{item2.align}}"{{#}}}>', '{{# if(item2.type === "checkbox"){ }}', '<input type="checkbox" name="layTableCheckbox" lay-skin="primary" lay-filter="layTableAllChoose" {{# if(item2[d.data.checkName]){ }}checked{{# }; }}>', "{{# } else { }}", '<span>{{item2.title||""}}</span>', "{{# if(isSort){ }}", '<span class="layui-table-sort layui-inline"><i class="layui-edge layui-table-sort-asc" title="升序"></i><i class="layui-edge layui-table-sort-desc" title="降序"></i></span>', "{{# } }}", "{{# } }}", "</div>", "</th>", e.fixed ? "{{# }; }}" : "", "{{# }); }}", "</tr>", "{{# }); }}", "</thead>", "</table>"].join("")
        },
        z = ['<table cellspacing="0" cellpadding="0" border="0" class="layui-table" ', '{{# if(d.data.skin){ }}lay-skin="{{d.data.skin}}"{{# } }} {{# if(d.data.size){ }}lay-size="{{d.data.size}}"{{# } }} {{# if(d.data.even){ }}lay-even{{# } }}>', "<tbody></tbody>", "</table>"].join(""),
        E = ['<div class="layui-form layui-border-box {{d.VIEW_CLASS}}" lay-filter="LAY-table-{{d.index}}" lay-id="{{ d.data.id }}" style="{{# if(d.data.width){ }}width:{{d.data.width}}px;{{# } }} {{# if(d.data.height){ }}height:{{d.data.height}}px;{{# } }}">', "{{# if(d.data.toolbar){ }}", '<div class="layui-table-tool">', '<div class="layui-table-tool-temp"></div>', '<div class="layui-table-tool-self"></div>', "</div>", "{{# } }}", '<div class="layui-table-box">', "{{# if(d.data.loading){ }}", '<div class="layui-table-init" style="background-color: #fff;">', '<i class="layui-icon layui-icon-loading layui-anim layui-anim-rotate layui-anim-loop"></i>', "</div>", "{{# } }}", "{{# var left, right; }}", '<div class="layui-table-header">', _(), "</div>", '<div class="layui-table-body layui-table-main">', z, "</div>", "{{# if(left){ }}", '<div class="layui-table-fixed layui-table-fixed-l">', '<div class="layui-table-header">', _({fixed: !0}), "</div>", '<div class="layui-table-body">', z, "</div>", "</div>", "{{# }; }}", "{{# if(right){ }}", '<div class="layui-table-fixed layui-table-fixed-r">', '<div class="layui-table-header">', _({fixed: "right"}), '<div class="layui-table-mend"></div>', "</div>", '<div class="layui-table-body">', z, "</div>", "</div>", "{{# }; }}", "</div>", "{{# if(d.data.totalRow){ }}", '<div class="layui-table-total">', '<table cellspacing="0" cellpadding="0" border="0" class="layui-table" ', '{{# if(d.data.skin){ }}lay-skin="{{d.data.skin}}"{{# } }} {{# if(d.data.size){ }}lay-size="{{d.data.size}}"{{# } }} {{# if(d.data.even){ }}lay-even{{# } }}>', '<tbody><tr><td><div class="layui-table-cell" style="visibility: hidden;">Total</div></td></tr></tbody>', "</table>", "</div>", "{{# } }}", "{{# if(d.data.page){ }}", '<div class="layui-table-page">', '<div id="layui-table-page{{d.index}}"></div>', "</div>", "{{# } }}", "<style>", "{{# layui.each(d.data.cols, function(i1, item1){", "layui.each(item1, function(i2, item2){ }}", ".laytable-cell-{{d.index}}-{{i1}}-{{i2}}{ ", "{{# if(item2.width){ }}", "width: {{item2.width}}px;", "{{# } }}", " }", "{{# });", "}); }}", "</style>", "</div>"].join(""),
        j = t(window), F = t(document), I = function (e) {
            var i = this;
            i.index = ++d.index, i.config = t.extend({}, i.config, d.config, e), i.render()
        };
    I.prototype.config = {
        limit: 10,
        loading: !0,
        cellMinWidth: 60,
        defaultToolbar: ["filter", "exports", "print"],
        autoSort: !0,
        text: {none: "无数据"}
    }, I.prototype.render = function () {
        var e = this, a = e.config;
        if (a.elem = t(a.elem), a.where = a.where || {}, a.id = a.id || a.elem.attr("id") || e.index, a.request = t.extend({
            pageName: "page",
            limitName: "limit"
        }, a.request), a.response = t.extend({
            statusName: "code",
            statusCode: 0,
            msgName: "msg",
            dataName: "data",
            totalRowName: "totalRow",
            countName: "count"
        }, a.response), "object" == typeof a.page && (a.limit = a.page.limit || a.limit, a.limits = a.page.limits || a.limits, e.page = a.page.curr = a.page.curr || 1, delete a.page.elem, delete a.page.jump), !a.elem[0]) return e;
        a.height && /^full-\d+$/.test(a.height) && (e.fullHeightGap = a.height.split("-")[1], a.height = j.height() - e.fullHeightGap), e.setInit();
        var l = a.elem, n = l.next("." + v), o = e.elem = t(i(E).render({VIEW_CLASS: v, data: a, index: e.index}));
        if (a.index = e.index, e.key = a.id || a.index, n[0] && n.remove(), l.after(o), e.layTool = o.find(m), e.layBox = o.find(g), e.layHeader = o.find(x), e.layMain = o.find(C), e.layBody = o.find(k), e.layFixed = o.find(w), e.layFixLeft = o.find(T), e.layFixRight = o.find(N), e.layTotal = o.find(A), e.layPage = o.find(L), e.renderToolbar(), e.fullSize(), a.cols.length > 1) {
            var r = e.layFixed.find(x).find("th");
            r.height(e.layHeader.height() - 1 - parseFloat(r.css("padding-top")) - parseFloat(r.css("padding-bottom")))
        }
        e.pullData(e.page), e.events()
    }, I.prototype.initOpts = function (e) {
        var t = this, i = (t.config, {checkbox: 48, radio: 48, space: 15, numbers: 40});
        e.checkbox && (e.type = "checkbox"), e.space && (e.type = "space"), e.type || (e.type = "normal"), "normal" !== e.type && (e.unresize = !0, e.width = e.width || i[e.type])
    }, I.prototype.setInit = function (e) {
        var t = this, i = t.config;
        return i.clientWidth = i.width || function () {
            var e = function (t) {
                var a, l;
                t = t || i.elem.parent(), a = t.width();
                try {
                    l = "none" === t.css("display")
                } catch (n) {
                }
                return !t[0] || a && !l ? a : e(t.parent())
            };
            return e()
        }(), "width" === e ? i.clientWidth : void layui.each(i.cols, function (e, a) {
            layui.each(a, function (l, n) {
                if (!n) return void a.splice(l, 1);
                if (n.key = e + "-" + l, n.hide = n.hide || !1, n.colGroup || n.colspan > 1) {
                    var o = 0;
                    layui.each(i.cols[e + 1], function (t, i) {
                        i.HAS_PARENT || o > 1 && o == n.colspan || (i.HAS_PARENT = !0, i.parentKey = e + "-" + l, o += parseInt(i.colspan > 1 ? i.colspan : 1))
                    }), n.colGroup = !0
                }
                t.initOpts(n)
            })
        })
    }, I.prototype.renderToolbar = function () {
        var e = this, a = e.config,
            l = ['<div class="layui-inline" lay-event="add"><i class="layui-icon layui-icon-add-1"></i></div>', '<div class="layui-inline" lay-event="update"><i class="layui-icon layui-icon-edit"></i></div>', '<div class="layui-inline" lay-event="delete"><i class="layui-icon layui-icon-delete"></i></div>'].join(""),
            n = e.layTool.find(".layui-table-tool-temp");
        if ("default" === a.toolbar) n.html(l); else if ("string" == typeof a.toolbar) {
            var o = t(a.toolbar).html() || "";
            o && n.html(i(o).render(a))
        }
        var r = {
            filter: {title: "筛选列", layEvent: "LAYTABLE_COLS", icon: "layui-icon-cols"},
            exports: {title: "导出", layEvent: "LAYTABLE_EXPORT", icon: "layui-icon-export"},
            print: {title: "打印", layEvent: "LAYTABLE_PRINT", icon: "layui-icon-print"}
        }, d = [];
        "object" == typeof a.defaultToolbar && layui.each(a.defaultToolbar, function (e, t) {
            var i = "string" == typeof t ? r[t] : t;
            i && d.push('<div class="layui-inline" title="' + i.title + '" lay-event="' + i.layEvent + '"><i class="layui-icon ' + i.icon + '"></i></div>')
        }), e.layTool.find(".layui-table-tool-self").html(d.join(""))
    }, I.prototype.setParentCol = function (e, t) {
        var i = this, a = i.config, l = i.layHeader.find('th[data-key="' + a.index + "-" + t + '"]'),
            n = parseInt(l.attr("colspan")) || 0;
        if (l[0]) {
            var o = t.split("-"), r = a.cols[o[0]][o[1]];
            e ? n-- : n++, l.attr("colspan", n), l[n < 1 ? "addClass" : "removeClass"](f), r.colspan = n, r.hide = n < 1;
            var d = l.data("parentkey");
            d && i.setParentCol(e, d)
        }
    }, I.prototype.setColsPatch = function () {
        var e = this, t = e.config;
        layui.each(t.cols, function (t, i) {
            layui.each(i, function (t, i) {
                i.hide && e.setParentCol(i.hide, i.parentKey)
            })
        })
    }, I.prototype.setColsWidth = function () {
        var e = this, t = e.config, i = 0, a = 0, l = 0, n = 0, o = e.setInit("width");
        e.eachCols(function (e, t) {
            t.hide || i++
        }), o = o - function () {
            return "line" === t.skin || "nob" === t.skin ? 2 : i + 1
        }() - e.getScrollWidth(e.layMain[0]) - 1;
        var r = function (e) {
            layui.each(t.cols, function (i, r) {
                layui.each(r, function (i, d) {
                    var c = 0, s = d.minWidth || t.cellMinWidth;
                    return d ? void (d.colGroup || d.hide || (e ? l && l < s && (a--, c = s) : (c = d.width || 0, /\d+%$/.test(c) ? (c = Math.floor(parseFloat(c) / 100 * o), c < s && (c = s)) : c || (d.width = c = 0, a++)), d.hide && (c = 0), n += c)) : void r.splice(i, 1)
                })
            }), o > n && a && (l = (o - n) / a)
        };
        r(), r(!0), e.autoColNums = a, e.eachCols(function (i, a) {
            var n = a.minWidth || t.cellMinWidth;
            a.colGroup || a.hide || (0 === a.width ? e.getCssRule(t.index + "-" + a.key, function (e) {
                e.style.width = Math.floor(l >= n ? l : n) + "px"
            }) : /\d+%$/.test(a.width) && e.getCssRule(t.index + "-" + a.key, function (e) {
                e.style.width = Math.floor(parseFloat(a.width) / 100 * o) + "px"
            }))
        });
        var d = e.layMain.width() - e.getScrollWidth(e.layMain[0]) - e.layMain.children("table").outerWidth();
        if (e.autoColNums && d >= -i && d <= i) {
            var c = function (t) {
                var i;
                return t = t || e.layHeader.eq(0).find("thead th:last-child"), i = t.data("field"), !i && t.prev()[0] ? c(t.prev()) : t
            }, s = c(), u = s.data("key");
            e.getCssRule(u, function (t) {
                var i = t.style.width || s.outerWidth();
                t.style.width = parseFloat(i) + d + "px", e.layMain.height() - e.layMain.prop("clientHeight") > 0 && (t.style.width = parseFloat(t.style.width) - 1 + "px")
            })
        }
        e.loading(!0)
    }, I.prototype.resize = function () {
        var e = this;
        e.fullSize(), e.setColsWidth(), e.scrollPatch()
    }, I.prototype.reload = function (e, i) {
        var a = this;
        e = e || {}, delete a.haveInit, e.data && e.data.constructor === Array && delete a.config.data, a.config = t.extend(i, {}, a.config, e), a.render()
    }, I.prototype.errorView = function (e) {
        var i = this, a = i.layMain.find("." + p), l = t('<div class="' + p + '">' + (e || "Error") + "</div>");
        a[0] && (i.layNone.remove(), a.remove()), i.layFixed.addClass(f), i.layMain.find("tbody").html(""), i.layMain.append(i.layNone = l), d.cache[i.key] = []
    }, I.prototype.page = 1, I.prototype.pullData = function (e) {
        var i = this, a = i.config, l = a.request, n = a.response, o = function () {
            "object" == typeof a.initSort && i.sort(a.initSort.field, a.initSort.type)
        };
        if (i.startTime = (new Date).getTime(), a.url) {
            var r = {};
            r[l.pageName] = e, r[l.limitName] = a.limit;
            var d = t.extend(r, a.where);
            a.contentType && 0 == a.contentType.indexOf("application/json") && (d = JSON.stringify(d)), i.loading(), t.ajax({
                type: a.method || "get",
                url: a.url,
                contentType: a.contentType,
                data: d,
                dataType: "json",
                headers: a.headers || {},
                success: function (t) {
                    "function" == typeof a.parseData && (t = a.parseData(t) || t), t[n.statusName] != n.statusCode ? (i.renderForm(), i.errorView(t[n.msgName] || '返回的数据不符合规范，正确的成功状态码应为："' + n.statusName + '": ' + n.statusCode)) : (i.renderData(t, e, t[n.countName]), o(), a.time = (new Date).getTime() - i.startTime + " ms"), i.setColsWidth(), "function" == typeof a.done && a.done(t, e, t[n.countName])
                },
                error: function (e, t) {
                    i.errorView("数据接口请求异常：" + t), i.renderForm(), i.setColsWidth(), "function" == typeof a.error && a.error(e, t)
                }
            })
        } else if (a.data && a.data.constructor === Array) {
            var c = {}, s = e * a.limit - a.limit;
            c[n.dataName] = a.data.concat().splice(s, a.limit), c[n.countName] = a.data.length, "object" == typeof a.totalRow && (c[n.totalRowName] = t.extend({}, a.totalRow)), i.renderData(c, e, c[n.countName]), o(), i.setColsWidth(), "function" == typeof a.done && a.done(c, e, c[n.countName])
        }
    }, I.prototype.eachCols = function (e) {
        var t = this;
        return d.eachCols(null, e, t.config.cols), t
    }, I.prototype.renderData = function (e, n, o, r) {
        var c = this, s = c.config, y = e[s.response.dataName] || [], h = e[s.response.totalRowName], v = [], m = [],
            g = [], b = function () {
                var e;
                return !r && c.sortKey ? c.sort(c.sortKey.field, c.sortKey.sort, !0) : (layui.each(y, function (a, l) {
                    var o = [], y = [], h = [], p = a + s.limit * (n - 1) + 1;
                    0 !== l.length && (r || (l[d.config.indexName] = a), c.eachCols(function (n, r) {
                        var c = r.field || n, v = s.index + "-" + r.key, m = l[c];
                        if (void 0 !== m && null !== m || (m = ""), !r.colGroup) {
                            var g = ['<td data-field="' + c + '" data-key="' + v + '" ' + function () {
                                var e = [];
                                return r.edit && e.push('data-edit="' + r.edit + '"'), r.align && e.push('align="' + r.align + '"'), r.templet && e.push('data-content="' + m + '"'), r.toolbar && e.push('data-off="true"'), r.event && e.push('lay-event="' + r.event + '"'), r.style && e.push('style="' + r.style + '"'), r.minWidth && e.push('data-minwidth="' + r.minWidth + '"'), e.join(" ")
                            }() + ' class="' + function () {
                                var e = [];
                                return r.hide && e.push(f), r.field || e.push("layui-table-col-special"), e.join(" ")
                            }() + '">', '<div class="layui-table-cell laytable-cell-' + function () {
                                return "normal" === r.type ? v : v + " laytable-cell-" + r.type
                            }() + '">' + function () {
                                var n = t.extend(!0, {LAY_INDEX: p}, l), o = d.config.checkName;
                                switch (r.type) {
                                    case"checkbox":
                                        return '<input type="checkbox" name="layTableCheckbox" lay-skin="primary" ' + function () {
                                            return r[o] ? (l[o] = r[o], r[o] ? "checked" : "") : n[o] ? "checked" : ""
                                        }() + ">";
                                    case"radio":
                                        return n[o] && (e = a), '<input type="radio" name="layTableRadio_' + s.index + '" ' + (n[o] ? "checked" : "") + ' lay-type="layTableRadio">';
                                    case"numbers":
                                        return p
                                }
                                return r.toolbar ? i(t(r.toolbar).html() || "").render(n) : u(r, m, n)
                            }(), "</div></td>"].join("");
                            o.push(g), r.fixed && "right" !== r.fixed && y.push(g), "right" === r.fixed && h.push(g)
                        }
                    }), v.push('<tr data-index="' + a + '">' + o.join("") + "</tr>"), m.push('<tr data-index="' + a + '">' + y.join("") + "</tr>"), g.push('<tr data-index="' + a + '">' + h.join("") + "</tr>"))
                }), c.layBody.scrollTop(0), c.layMain.find("." + p).remove(), c.layMain.find("tbody").html(v.join("")), c.layFixLeft.find("tbody").html(m.join("")), c.layFixRight.find("tbody").html(g.join("")), c.renderForm(), "number" == typeof e && c.setThisRowChecked(e), c.syncCheckAll(), c.haveInit ? c.scrollPatch() : setTimeout(function () {
                    c.scrollPatch()
                }, 50), c.haveInit = !0, l.close(c.tipsIndex), s.HAS_SET_COLS_PATCH || c.setColsPatch(), void (s.HAS_SET_COLS_PATCH = !0))
            };
        return d.cache[c.key] = y, c.layPage[0 == o || 0 === y.length && 1 == n ? "addClass" : "removeClass"](f), 0 === y.length ? (c.renderForm(), c.errorView(s.text.none)) : (c.layFixed.removeClass(f), r ? b() : (b(), c.renderTotal(y, h), void (s.page && (s.page = t.extend({
            elem: "layui-table-page" + s.index,
            count: o,
            limit: s.limit,
            limits: s.limits || [10, 20, 30, 40, 50, 60, 70, 80, 90],
            groups: 3,
            layout: ["prev", "page", "next", "skip", "count", "limit"],
            prev: '<i class="layui-icon">&#xe603;</i>',
            next: '<i class="layui-icon">&#xe602;</i>',
            jump: function (e, t) {
                t || (c.page = e.curr, s.limit = e.limit, c.pullData(e.curr))
            }
        }, s.page), s.page.count = o, a.render(s.page)))))
    }, I.prototype.renderTotal = function (e, a) {
        var l = this, n = l.config, o = {};
        if (n.totalRow) {
            layui.each(e, function (e, t) {
                0 !== t.length && l.eachCols(function (e, i) {
                    var a = i.field || e, l = t[a];
                    i.totalRow && (o[a] = (o[a] || 0) + (parseFloat(l) || 0))
                })
            }), l.dataTotal = {};
            var r = [];
            l.eachCols(function (e, d) {
                var c = d.field || e, s = function () {
                    var e, t = d.totalRowText || "", i = parseFloat(o[c]).toFixed(2), l = {};
                    return l[c] = i, e = d.totalRow ? u(d, i, l) || t : t, a ? a[d.field] || e : e
                }(), y = ['<td data-field="' + c + '" data-key="' + n.index + "-" + d.key + '" ' + function () {
                    var e = [];
                    return d.align && e.push('align="' + d.align + '"'), d.style && e.push('style="' + d.style + '"'), d.minWidth && e.push('data-minwidth="' + d.minWidth + '"'), e.join(" ")
                }() + ' class="' + function () {
                    var e = [];
                    return d.hide && e.push(f), d.field || e.push("layui-table-col-special"), e.join(" ")
                }() + '">', '<div class="layui-table-cell laytable-cell-' + function () {
                    var e = n.index + "-" + d.key;
                    return "normal" === d.type ? e : e + " laytable-cell-" + d.type
                }() + '">' + function () {
                    var e = d.totalRow || n.totalRow;
                    return "string" == typeof e ? i(e).render(t.extend({TOTAL_NUMS: s}, d)) : s
                }(), "</div></td>"].join("");
                d.field && (l.dataTotal[c] = s), r.push(y)
            }), l.layTotal.find("tbody").html("<tr>" + r.join("") + "</tr>")
        }
    }, I.prototype.getColElem = function (e, t) {
        var i = this, a = i.config;
        return e.eq(0).find(".laytable-cell-" + (a.index + "-" + t) + ":eq(0)")
    }, I.prototype.renderForm = function (e) {
        n.render(e, "LAY-table-" + this.index)
    }, I.prototype.setThisRowChecked = function (e) {
        var t = this, i = (t.config, "layui-table-click"), a = t.layBody.find('tr[data-index="' + e + '"]');
        a.addClass(i).siblings("tr").removeClass(i)
    }, I.prototype.sort = function (e, i, a, l) {
        var n, r, c = this, s = {}, u = c.config, h = u.elem.attr("lay-filter"), f = d.cache[c.key];
        "string" == typeof e && (n = e, c.layHeader.find("th").each(function (i, a) {
            var l = t(this), o = l.data("field");
            if (o === e) return e = l, n = o, !1
        }));
        try {
            var n = n || e.data("field"), p = e.data("key");
            if (c.sortKey && !a && n === c.sortKey.field && i === c.sortKey.sort) return;
            var v = c.layHeader.find("th .laytable-cell-" + p).find(S);
            c.layHeader.find("th").find(S).removeAttr("lay-sort"), v.attr("lay-sort", i || null), c.layFixed.find("th")
        } catch (m) {
            o.error("Table modules: sort field '" + n + "' not matched")
        }
        c.sortKey = {
            field: n,
            sort: i
        }, u.autoSort && ("asc" === i ? r = layui.sort(f, n) : "desc" === i ? r = layui.sort(f, n, !0) : (r = layui.sort(f, d.config.indexName), delete c.sortKey)), s[u.response.dataName] = r || f, c.renderData(s, c.page, c.count, !0), l && layui.event.call(e, y, "sort(" + h + ")", {
            field: n,
            type: i
        })
    }, I.prototype.loading = function (e) {
        var i = this, a = i.config;
        a.loading && (e ? (i.layInit && i.layInit.remove(), delete i.layInit, i.layBox.find(b).remove()) : (i.layInit = t(['<div class="layui-table-init">', '<i class="layui-icon layui-icon-loading layui-anim layui-anim-rotate layui-anim-loop"></i>', "</div>"].join("")), i.layBox.append(i.layInit)))
    }, I.prototype.setCheckData = function (e, t) {
        var i = this, a = i.config, l = d.cache[i.key];
        l[e] && l[e].constructor !== Array && (l[e][a.checkName] = t)
    }, I.prototype.syncCheckAll = function () {
        var e = this, t = e.config, i = e.layHeader.find('input[name="layTableCheckbox"]'), a = function (i) {
            return e.eachCols(function (e, a) {
                "checkbox" === a.type && (a[t.checkName] = i)
            }), i
        };
        i[0] && (d.checkStatus(e.key).isAll ? (i[0].checked || (i.prop("checked", !0), e.renderForm("checkbox")), a(!0)) : (i[0].checked && (i.prop("checked", !1), e.renderForm("checkbox")), a(!1)))
    }, I.prototype.getCssRule = function (e, t) {
        var i = this, a = i.elem.find("style")[0], l = a.sheet || a.styleSheet || {}, n = l.cssRules || l.rules;
        layui.each(n, function (i, a) {
            if (a.selectorText === ".laytable-cell-" + e) return t(a), !0
        })
    }, I.prototype.fullSize = function () {
        var e, t = this, i = t.config, a = i.height;
        t.fullHeightGap && (a = j.height() - t.fullHeightGap, a < 135 && (a = 135), t.elem.css("height", a)), a && (e = parseFloat(a) - (t.layHeader.outerHeight() || 38), i.toolbar && (e -= t.layTool.outerHeight() || 50), i.totalRow && (e -= t.layTotal.outerHeight() || 40), i.page && (e -= t.layPage.outerHeight() || 41), t.layMain.css("height", e - 2))
    }, I.prototype.getScrollWidth = function (e) {
        var t = 0;
        return e ? t = e.offsetWidth - e.clientWidth : (e = document.createElement("div"), e.style.width = "100px", e.style.height = "100px", e.style.overflowY = "scroll", document.body.appendChild(e), t = e.offsetWidth - e.clientWidth, document.body.removeChild(e)), t
    }, I.prototype.scrollPatch = function () {
        var e = this, i = e.layMain.children("table"), a = e.layMain.width() - e.layMain.prop("clientWidth"),
            l = e.layMain.height() - e.layMain.prop("clientHeight"),
            n = (e.getScrollWidth(e.layMain[0]), i.outerWidth() - e.layMain.width()), o = function (e) {
                if (a && l) {
                    if (e = e.eq(0), !e.find(".layui-table-patch")[0]) {
                        var i = t('<th class="layui-table-patch"><div class="layui-table-cell"></div></th>');
                        i.find("div").css({width: a}), e.find("tr").append(i)
                    }
                } else e.find(".layui-table-patch").remove()
            };
        o(e.layHeader), o(e.layTotal);
        var r = e.layMain.height(), d = r - l;
        e.layFixed.find(k).css("height", i.height() >= d ? d : "auto"), e.layFixRight[n > 0 ? "removeClass" : "addClass"](f), e.layFixRight.css("right", a - 1)
    }, I.prototype.events = function () {
        var e, i = this, a = i.config, o = t("body"), c = {}, s = i.layHeader.find("th"), h = ".layui-table-cell",
            p = a.elem.attr("lay-filter");
        i.layTool.on("click", "*[lay-event]", function (e) {
            var o = t(this), c = o.attr("lay-event"), s = function (e) {
                var l = t(e.list), n = t('<ul class="layui-table-tool-panel"></ul>');
                n.html(l), a.height && n.css("max-height", a.height - (i.layTool.outerHeight() || 50)), o.find(".layui-table-tool-panel")[0] || o.append(n), i.renderForm(), n.on("click", function (e) {
                    layui.stope(e)
                }), e.done && e.done(n, l)
            };
            switch (layui.stope(e), F.trigger("table.tool.panel.remove"), l.close(i.tipsIndex), c) {
                case"LAYTABLE_COLS":
                    s({
                        list: function () {
                            var e = [];
                            return i.eachCols(function (t, i) {
                                i.field && "normal" == i.type && e.push('<li><input type="checkbox" name="' + i.field + '" data-key="' + i.key + '" data-parentkey="' + (i.parentKey || "") + '" lay-skin="primary" ' + (i.hide ? "" : "checked") + ' title="' + (i.title || i.field) + '" lay-filter="LAY_TABLE_TOOL_COLS"></li>')
                            }), e.join("")
                        }(), done: function () {
                            n.on("checkbox(LAY_TABLE_TOOL_COLS)", function (e) {
                                var l = t(e.elem), n = this.checked, o = l.data("key"), r = l.data("parentkey");
                                layui.each(a.cols, function (e, t) {
                                    layui.each(t, function (t, l) {
                                        if (e + "-" + t === o) {
                                            var d = l.hide;
                                            l.hide = !n, i.elem.find('*[data-key="' + a.index + "-" + o + '"]')[n ? "removeClass" : "addClass"](f), d != l.hide && i.setParentCol(!n, r), i.resize()
                                        }
                                    })
                                })
                            })
                        }
                    });
                    break;
                case"LAYTABLE_EXPORT":
                    r.ie ? l.tips("导出功能不支持 IE，请用 Chrome 等高级浏览器导出", this, {tips: 3}) : s({
                        list: function () {
                            return ['<li data-type="csv">导出到 Csv 文件</li>', '<li data-type="xls">导出到 Excel 文件</li>'].join("")
                        }(), done: function (e, l) {
                            l.on("click", function () {
                                var e = t(this).data("type");
                                d.exportFile.call(i, a.id, null, e)
                            })
                        }
                    });
                    break;
                case"LAYTABLE_PRINT":
                    var u = window.open("打印窗口", "_blank"),
                        h = ["<style>", "body{font-size: 12px; color: #666;}", "table{width: 100%; border-collapse: collapse; border-spacing: 0;}", "th,td{line-height: 20px; padding: 9px 15px; border: 1px solid #ccc; text-align: left; font-size: 12px; color: #666;}", "a{color: #666; text-decoration:none;}", "*.layui-hide{display: none}", "</style>"].join(""),
                        v = t(i.layHeader.html());
                    v.append(i.layMain.find("table").html()), v.append(i.layTotal.find("table").html()), v.find("th.layui-table-patch").remove(), v.find(".layui-table-col-special").remove(), u.document.write(h + v.prop("outerHTML")), u.document.close(), u.print(), u.close()
            }
            layui.event.call(this, y, "toolbar(" + p + ")", t.extend({event: c, config: a}, {}))
        }), s.on("mousemove", function (e) {
            var i = t(this), a = i.offset().left, l = e.clientX - a;
            i.data("unresize") || c.resizeStart || (c.allowResize = i.width() - l <= 10, o.css("cursor", c.allowResize ? "col-resize" : ""))
        }).on("mouseleave", function () {
            t(this);
            c.resizeStart || o.css("cursor", "")
        }).on("mousedown", function (e) {
            var l = t(this);
            if (c.allowResize) {
                var n = l.data("key");
                e.preventDefault(), c.resizeStart = !0, c.offset = [e.clientX, e.clientY], i.getCssRule(n, function (e) {
                    var t = e.style.width || l.outerWidth();
                    c.rule = e, c.ruleWidth = parseFloat(t), c.minWidth = l.data("minwidth") || a.cellMinWidth
                })
            }
        }), F.on("mousemove", function (t) {
            if (c.resizeStart) {
                if (t.preventDefault(), c.rule) {
                    var a = c.ruleWidth + t.clientX - c.offset[0];
                    a < c.minWidth && (a = c.minWidth), c.rule.style.width = a + "px", l.close(i.tipsIndex)
                }
                e = 1
            }
        }).on("mouseup", function (t) {
            c.resizeStart && (c = {}, o.css("cursor", ""), i.scrollPatch()), 2 === e && (e = null)
        }), s.on("click", function (a) {
            var l, n = t(this), o = n.find(S), r = o.attr("lay-sort");
            return o[0] && 1 !== e ? (l = "asc" === r ? "desc" : "desc" === r ? null : "asc", void i.sort(n, l, null, !0)) : e = 2
        }).find(S + " .layui-edge ").on("click", function (e) {
            var a = t(this), l = a.index(), n = a.parents("th").eq(0).data("field");
            layui.stope(e), 0 === l ? i.sort(n, "asc", null, !0) : i.sort(n, "desc", null, !0)
        });
        var v = function (e) {
            var a = t(this), l = a.parents("tr").eq(0).data("index"), n = i.layBody.find('tr[data-index="' + l + '"]'),
                o = d.cache[i.key] || [];
            return o = o[l] || {}, t.extend({
                tr: n, data: d.clearCacheKey(o), del: function () {
                    d.cache[i.key][l] = [], n.remove(), i.scrollPatch()
                }, update: function (e) {
                    e = e || {}, layui.each(e, function (e, t) {
                        if (e in o) {
                            var a, l = n.children('td[data-field="' + e + '"]');
                            o[e] = t, i.eachCols(function (t, i) {
                                i.field == e && i.templet && (a = i.templet)
                            }), l.children(h).html(u({templet: a}, t, o)), l.data("content", t)
                        }
                    })
                }
            }, e)
        };
        i.elem.on("click", 'input[name="layTableCheckbox"]+', function () {
            var e = t(this).prev(), a = i.layBody.find('input[name="layTableCheckbox"]'),
                l = e.parents("tr").eq(0).data("index"), n = e[0].checked,
                o = "layTableAllChoose" === e.attr("lay-filter");
            o ? (a.each(function (e, t) {
                t.checked = n, i.setCheckData(e, n)
            }), i.syncCheckAll(), i.renderForm("checkbox")) : (i.setCheckData(l, n), i.syncCheckAll()), layui.event.call(e[0], y, "checkbox(" + p + ")", v.call(e[0], {
                checked: n,
                type: o ? "all" : "one"
            }))
        }), i.elem.on("click", 'input[lay-type="layTableRadio"]+', function () {
            var e = t(this).prev(), l = e[0].checked, n = d.cache[i.key], o = e.parents("tr").eq(0).data("index");
            layui.each(n, function (e, t) {
                o === e ? t[a.checkName] = !0 : delete t[a.checkName]
            }), i.setThisRowChecked(o), layui.event.call(this, y, "radio(" + p + ")", v.call(this, {checked: l}))
        }), i.layBody.on("mouseenter", "tr", function () {
            var e = t(this), a = e.index();
            e.data("off") || i.layBody.find("tr:eq(" + a + ")").addClass(W)
        }).on("mouseleave", "tr", function () {
            var e = t(this), a = e.index();
            e.data("off") || i.layBody.find("tr:eq(" + a + ")").removeClass(W)
        }).on("click", "tr", function () {
            m.call(this, "row")
        }).on("dblclick", "tr", function () {
            m.call(this, "rowDouble")
        });
        var m = function (e) {
            var i = t(this);
            i.data("off") || layui.event.call(this, y, e + "(" + p + ")", v.call(i.children("td")[0]))
        };
        i.layBody.on("change", "." + R, function () {
            var e = t(this), a = this.value, l = e.parent().data("field"), n = e.parents("tr").eq(0).data("index"),
                o = d.cache[i.key][n];
            o[l] = a, layui.event.call(this, y, "edit(" + p + ")", v.call(this, {value: a, field: l}))
        }).on("blur", "." + R, function () {
            var e, a = t(this), l = this, n = a.parent().data("field"), o = a.parents("tr").eq(0).data("index"),
                r = d.cache[i.key][o];
            i.eachCols(function (t, i) {
                i.field == n && i.templet && (e = i.templet)
            }), a.siblings(h).html(function (t) {
                return u({templet: e}, t, r)
            }(l.value)), a.parent().data("content", l.value), a.remove()
        }), i.layBody.on("click", "td", function (e) {
            var i = t(this), a = (i.data("field"), i.data("edit")), l = i.children(h);
            if (!i.data("off") && a) {
                var n = t('<input class="layui-input ' + R + '">');
                return n[0].value = i.data("content") || l.text(), i.find("." + R)[0] || i.append(n), n.focus(), void layui.stope(e)
            }
        }).on("mouseenter", "td", function () {
            b.call(this)
        }).on("mouseleave", "td", function () {
            b.call(this, "hide")
        });
        var g = "layui-table-grid-down", b = function (e) {
            var i = t(this), a = i.children(h);
            if (!i.data("off")) if (e) i.find(".layui-table-grid-down").remove(); else if (a.prop("scrollWidth") > a.outerWidth()) {
                if (a.find("." + g)[0]) return;
                i.append('<div class="' + g + '"><i class="layui-icon layui-icon-down"></i></div>')
            }
        };
        i.layBody.on("click", "." + g, function (e) {
            var n = t(this), o = n.parent(), d = o.children(h);
            i.tipsIndex = l.tips(['<div class="layui-table-tips-main" style="margin-top: -' + (d.height() + 16) + "px;" + function () {
                return "sm" === a.size ? "padding: 4px 15px; font-size: 12px;" : "lg" === a.size ? "padding: 14px 15px;" : ""
            }() + '">', d.html(), "</div>", '<i class="layui-icon layui-table-tips-c layui-icon-close"></i>'].join(""), d[0], {
                tips: [3, ""],
                time: -1,
                anim: -1,
                maxWidth: r.ios || r.android ? 300 : i.elem.width() / 2,
                isOutAnim: !1,
                skin: "layui-table-tips",
                success: function (e, t) {
                    e.find(".layui-table-tips-c").on("click", function () {
                        l.close(t)
                    })
                }
            }), layui.stope(e)
        }), i.layBody.on("click", "*[lay-event]", function () {
            var e = t(this), a = e.parents("tr").eq(0).data("index");
            layui.event.call(this, y, "tool(" + p + ")", v.call(this, {event: e.attr("lay-event")})), i.setThisRowChecked(a)
        }), i.layMain.on("scroll", function () {
            var e = t(this), a = e.scrollLeft(), n = e.scrollTop();
            i.layHeader.scrollLeft(a), i.layTotal.scrollLeft(a), i.layFixed.find(k).scrollTop(n), l.close(i.tipsIndex)
        }), j.on("resize", function () {
            i.resize()
        })
    }, function () {
        F.on("click", function () {
            F.trigger("table.remove.tool.panel")
        }), F.on("table.remove.tool.panel", function () {
            t(".layui-table-tool-panel").remove()
        })
    }(), d.init = function (e, i) {
        i = i || {};
        var a = this, l = t(e ? 'table[lay-filter="' + e + '"]' : h + "[lay-data]"),
            n = "Table element property lay-data configuration item has a syntax error: ";
        return l.each(function () {
            var a = t(this), l = a.attr("lay-data");
            try {
                l = new Function("return " + l)()
            } catch (r) {
                o.error(n + l, "error")
            }
            var c = [], s = t.extend({
                elem: this,
                cols: [],
                data: [],
                skin: a.attr("lay-skin"),
                size: a.attr("lay-size"),
                even: "string" == typeof a.attr("lay-even")
            }, d.config, i, l);
            e && a.hide(), a.find("thead>tr").each(function (e) {
                s.cols[e] = [], t(this).children().each(function (i) {
                    var a = t(this), l = a.attr("lay-data");
                    try {
                        l = new Function("return " + l)()
                    } catch (r) {
                        return o.error(n + l)
                    }
                    var d = t.extend({
                        title: a.text(),
                        colspan: a.attr("colspan") || 0,
                        rowspan: a.attr("rowspan") || 0
                    }, l);
                    d.colspan < 2 && c.push(d), s.cols[e].push(d)
                })
            }), a.find("tbody>tr").each(function (e) {
                var i = t(this), a = {};
                i.children("td").each(function (e, i) {
                    var l = t(this), n = l.data("field");
                    if (n) return a[n] = l.html()
                }), layui.each(c, function (e, t) {
                    var l = i.children("td").eq(e);
                    a[t.field] = l.html()
                }), s.data[e] = a
            }), d.render(s)
        }), a
    }, c.that = {}, c.config = {}, d.eachCols = function (e, i, a) {
        var l = c.config[e] || {}, n = [], o = 0;
        a = t.extend(!0, [], a || l.cols), layui.each(a, function (e, t) {
            layui.each(t, function (t, i) {
                if (i.colGroup) {
                    var l = 0;
                    o++, i.CHILD_COLS = [], layui.each(a[e + 1], function (e, t) {
                        t.PARENT_COL_INDEX || l > 1 && l == i.colspan || (t.PARENT_COL_INDEX = o, i.CHILD_COLS.push(t), l += parseInt(t.colspan > 1 ? t.colspan : 1))
                    })
                }
                i.PARENT_COL_INDEX || n.push(i)
            })
        });
        var r = function (e) {
            layui.each(e || n, function (e, t) {
                return t.CHILD_COLS ? r(t.CHILD_COLS) : void ("function" == typeof i && i(e, t))
            })
        };
        r()
    }, d.checkStatus = function (e) {
        var t = 0, i = 0, a = [], l = d.cache[e] || [];
        return layui.each(l, function (e, l) {
            return l.constructor === Array ? void i++ : void (l[d.config.checkName] && (t++, a.push(d.clearCacheKey(l))))
        }), {data: a, isAll: !!l.length && t === l.length - i}
    }, d.getData = function (e) {
        var t = [], i = d.cache[e] || [];
        return layui.each(i, function (e, i) {
            i.constructor !== Array && t.push(d.clearCacheKey(i))
        }), t
    }, d.exportFile = function (e, t, i) {
        var a = this;
        t = t || d.clearCacheKey(d.cache[e]), i = i || "csv";
        var l = c.config[e] || {}, n = {csv: "text/csv", xls: "application/vnd.ms-excel"}[i],
            s = document.createElement("a");
        return r.ie ? o.error("IE_NOT_SUPPORT_EXPORTS") : (s.href = "data:" + n + ";charset=utf-8,\ufeff" + encodeURIComponent(function () {
            var i = [], l = [], n = [];
            return layui.each(t, function (t, a) {
                var n = [];
                "object" == typeof e ? (layui.each(e, function (e, a) {
                    0 == t && i.push(a || "")
                }), layui.each(d.clearCacheKey(a), function (e, t) {
                    n.push('"' + (t || "") + '"')
                })) : d.eachCols(e, function (e, l) {
                    if (l.field && "normal" == l.type && !l.hide) {
                        var o = a[l.field];
                        void 0 !== o && null !== o || (o = ""), 0 == t && i.push(l.title || ""), n.push('"' + u(l, o, a, "text") + '"')
                    }
                }), l.push(n.join(","))
            }), layui.each(a.dataTotal, function (e, t) {
                n.push(t)
            }), i.join(",") + "\r\n" + l.join("\r\n") + "\r\n" + n.join(",")
        }()), s.download = (l.title || "table_" + (l.index || "")) + "." + i, document.body.appendChild(s), s.click(), void document.body.removeChild(s))
    }, d.resize = function (e) {
        if (e) {
            var t = s(e);
            if (!t) return;
            c.that[e].resize()
        } else layui.each(c.that, function () {
            this.resize()
        })
    }, d.reload = function (e, t, i) {
        var a = s(e);
        if (a) {
            var l = c.that[e];
            return l.reload(t, i), c.call(l)
        }
    }, d.render = function (e) {
        var t = new I(e);
        return c.call(t)
    }, d.clearCacheKey = function (e) {
        return e = t.extend({}, e), delete e[d.config.checkName], delete e[d.config.indexName], e
    }, t(function () {
        d.init()
    }), e(y, d)
});
layui.define("jquery", function (e) {
    "use strict";
    var i = layui.$, n = (layui.hint(), layui.device(), {
            config: {}, set: function (e) {
                var n = this;
                return n.config = i.extend({}, n.config, e), n
            }, on: function (e, i) {
                return layui.onevent.call(this, t, e, i)
            }
        }), t = "carousel", a = "layui-this", l = ">*[carousel-item]>*", o = "layui-carousel-left",
        r = "layui-carousel-right", d = "layui-carousel-prev", s = "layui-carousel-next", u = "layui-carousel-arrow",
        c = "layui-carousel-ind", m = function (e) {
            var t = this;
            t.config = i.extend({}, t.config, n.config, e), t.render()
        };
    m.prototype.config = {
        width: "600px",
        height: "280px",
        full: !1,
        arrow: "hover",
        indicator: "inside",
        autoplay: !0,
        interval: 3e3,
        anim: "",
        trigger: "click",
        index: 0
    }, m.prototype.render = function () {
        var e = this, n = e.config;
        n.elem = i(n.elem), n.elem[0] && (e.elemItem = n.elem.find(l), n.index < 0 && (n.index = 0), n.index >= e.elemItem.length && (n.index = e.elemItem.length - 1), n.interval < 800 && (n.interval = 800), n.full ? n.elem.css({
            position: "fixed",
            width: "100%",
            height: "100%",
            zIndex: 9999
        }) : n.elem.css({
            width: n.width,
            height: n.height
        }), n.elem.attr("lay-anim", n.anim), e.elemItem.eq(n.index).addClass(a), e.elemItem.length <= 1 || (e.indicator(), e.arrow(), e.autoplay(), e.events()))
    }, m.prototype.reload = function (e) {
        var n = this;
        clearInterval(n.timer), n.config = i.extend({}, n.config, e), n.render()
    }, m.prototype.prevIndex = function () {
        var e = this, i = e.config, n = i.index - 1;
        return n < 0 && (n = e.elemItem.length - 1), n
    }, m.prototype.nextIndex = function () {
        var e = this, i = e.config, n = i.index + 1;
        return n >= e.elemItem.length && (n = 0), n
    }, m.prototype.addIndex = function (e) {
        var i = this, n = i.config;
        e = e || 1, n.index = n.index + e, n.index >= i.elemItem.length && (n.index = 0)
    }, m.prototype.subIndex = function (e) {
        var i = this, n = i.config;
        e = e || 1, n.index = n.index - e, n.index < 0 && (n.index = i.elemItem.length - 1)
    }, m.prototype.autoplay = function () {
        var e = this, i = e.config;
        i.autoplay && (clearInterval(e.timer), e.timer = setInterval(function () {
            e.slide()
        }, i.interval))
    }, m.prototype.arrow = function () {
        var e = this, n = e.config,
            t = i(['<button class="layui-icon ' + u + '" lay-type="sub">' + ("updown" === n.anim ? "&#xe619;" : "&#xe603;") + "</button>", '<button class="layui-icon ' + u + '" lay-type="add">' + ("updown" === n.anim ? "&#xe61a;" : "&#xe602;") + "</button>"].join(""));
        n.elem.attr("lay-arrow", n.arrow), n.elem.find("." + u)[0] && n.elem.find("." + u).remove(), n.elem.append(t), t.on("click", function () {
            var n = i(this), t = n.attr("lay-type");
            e.slide(t)
        })
    }, m.prototype.indicator = function () {
        var e = this, n = e.config, t = e.elemInd = i(['<div class="' + c + '"><ul>', function () {
            var i = [];
            return layui.each(e.elemItem, function (e) {
                i.push("<li" + (n.index === e ? ' class="layui-this"' : "") + "></li>")
            }), i.join("")
        }(), "</ul></div>"].join(""));
        n.elem.attr("lay-indicator", n.indicator), n.elem.find("." + c)[0] && n.elem.find("." + c).remove(), n.elem.append(t), "updown" === n.anim && t.css("margin-top", -(t.height() / 2)), t.find("li").on("hover" === n.trigger ? "mouseover" : n.trigger, function () {
            var t = i(this), a = t.index();
            a > n.index ? e.slide("add", a - n.index) : a < n.index && e.slide("sub", n.index - a)
        })
    }, m.prototype.slide = function (e, i) {
        var n = this, l = n.elemItem, u = n.config, c = u.index, m = u.elem.attr("lay-filter");
        n.haveSlide || ("sub" === e ? (n.subIndex(i), l.eq(u.index).addClass(d), setTimeout(function () {
            l.eq(c).addClass(r), l.eq(u.index).addClass(r)
        }, 50)) : (n.addIndex(i), l.eq(u.index).addClass(s), setTimeout(function () {
            l.eq(c).addClass(o), l.eq(u.index).addClass(o)
        }, 50)), setTimeout(function () {
            l.removeClass(a + " " + d + " " + s + " " + o + " " + r), l.eq(u.index).addClass(a), n.haveSlide = !1
        }, 300), n.elemInd.find("li").eq(u.index).addClass(a).siblings().removeClass(a), n.haveSlide = !0, layui.event.call(this, t, "change(" + m + ")", {
            index: u.index,
            prevIndex: c,
            item: l.eq(u.index)
        }))
    }, m.prototype.events = function () {
        var e = this, i = e.config;
        i.elem.data("haveEvents") || (i.elem.on("mouseenter", function () {
            clearInterval(e.timer)
        }).on("mouseleave", function () {
            e.autoplay()
        }), i.elem.data("haveEvents", !0))
    }, n.render = function (e) {
        var i = new m(e);
        return i
    }, e(t, n)
});
layui.define("jquery", function (e) {
    "use strict";
    var a = layui.jquery, l = {
            config: {}, index: layui.rate ? layui.rate.index + 1e4 : 0, set: function (e) {
                var l = this;
                return l.config = a.extend({}, l.config, e), l
            }, on: function (e, a) {
                return layui.onevent.call(this, n, e, a)
            }
        }, i = function () {
            var e = this, a = e.config;
            return {
                setvalue: function (a) {
                    e.setvalue.call(e, a)
                }, config: a
            }
        }, n = "rate", t = "layui-rate", o = "layui-icon-rate", u = "layui-icon-rate-solid", s = "layui-icon-rate-half",
        r = "layui-icon-rate-solid layui-icon-rate-half", c = "layui-icon-rate-solid layui-icon-rate",
        f = "layui-icon-rate layui-icon-rate-half", v = function (e) {
            var i = this;
            i.index = ++l.index, i.config = a.extend({}, i.config, l.config, e), i.render()
        };
    v.prototype.config = {
        length: 5,
        text: !1,
        readonly: !1,
        half: !1,
        value: 0,
        theme: ""
    }, v.prototype.render = function () {
        var e = this, l = e.config, i = l.theme ? 'style="color: ' + l.theme + ';"' : "";
        l.elem = a(l.elem), l.value > l.length && (l.value = l.length), parseInt(l.value) !== l.value && (l.half || (l.value = Math.ceil(l.value) - l.value < .5 ? Math.ceil(l.value) : Math.floor(l.value)));
        for (var n = '<ul class="layui-rate" ' + (l.readonly ? "readonly" : "") + ">", s = 1; s <= l.length; s++) {
            var r = '<li class="layui-inline"><i class="layui-icon ' + (s > Math.floor(l.value) ? o : u) + '" ' + i + "></i></li>";
            l.half && parseInt(l.value) !== l.value && s == Math.ceil(l.value) ? n = n + '<li><i class="layui-icon layui-icon-rate-half" ' + i + "></i></li>" : n += r
        }
        n += "</ul>" + (l.text ? '<span class="layui-inline">' + l.value + "星" : "") + "</span>";
        var c = l.elem, f = c.next("." + t);
        f[0] && f.remove(), e.elemTemp = a(n), l.span = e.elemTemp.next("span"), l.setText && l.setText(l.value), c.html(e.elemTemp), c.addClass("layui-inline"), l.readonly || e.action()
    }, v.prototype.setvalue = function (e) {
        var a = this, l = a.config;
        l.value = e, a.render()
    }, v.prototype.action = function () {
        var e = this, l = e.config, i = e.elemTemp, n = i.find("i").width();
        i.children("li").each(function (e) {
            var t = e + 1, v = a(this);
            v.on("click", function (e) {
                if (l.value = t, l.half) {
                    var o = e.pageX - a(this).offset().left;
                    o <= n / 2 && (l.value = l.value - .5)
                }
                l.text && i.next("span").text(l.value + "星"), l.choose && l.choose(l.value), l.setText && l.setText(l.value)
            }), v.on("mousemove", function (e) {
                if (i.find("i").each(function () {
                    a(this).addClass(o).removeClass(r)
                }), i.find("i:lt(" + t + ")").each(function () {
                    a(this).addClass(u).removeClass(f)
                }), l.half) {
                    var c = e.pageX - a(this).offset().left;
                    c <= n / 2 && v.children("i").addClass(s).removeClass(u)
                }
            }), v.on("mouseleave", function () {
                i.find("i").each(function () {
                    a(this).addClass(o).removeClass(r)
                }), i.find("i:lt(" + Math.floor(l.value) + ")").each(function () {
                    a(this).addClass(u).removeClass(f)
                }), l.half && parseInt(l.value) !== l.value && i.children("li:eq(" + Math.floor(l.value) + ")").children("i").addClass(s).removeClass(c)
            })
        })
    }, v.prototype.events = function () {
        var e = this;
        e.config
    }, l.render = function (e) {
        var a = new v(e);
        return i.call(a)
    }, e(n, l)
});
layui.define("jquery", function (e) {
    "use strict";
    var l = layui.$, o = function (e) {
    }, t = '<i class="layui-anim layui-anim-rotate layui-anim-loop layui-icon ">&#xe63e;</i>';
    o.prototype.load = function (e) {
        var o, i, n, r, a = this, c = 0;
        e = e || {};
        var f = l(e.elem);
        if (f[0]) {
            var m = l(e.scrollElem || document), u = e.mb || 50, s = !("isAuto" in e) || e.isAuto, v = e.end || "没有更多了",
                y = e.scrollElem && e.scrollElem !== document, d = "<cite>加载更多</cite>",
                h = l('<div class="layui-flow-more"><a href="javascript:;">' + d + "</a></div>");
            f.find(".layui-flow-more")[0] || f.append(h);
            var p = function (e, t) {
                e = l(e), h.before(e), t = 0 == t || null, t ? h.html(v) : h.find("a").html(d), i = t, o = null, n && n()
            }, g = function () {
                o = !0, h.find("a").html(t), "function" == typeof e.done && e.done(++c, p)
            };
            if (g(), h.find("a").on("click", function () {
                l(this);
                i || o || g()
            }), e.isLazyimg) var n = a.lazyimg({elem: e.elem + " img", scrollElem: e.scrollElem});
            return s ? (m.on("scroll", function () {
                var e = l(this), t = e.scrollTop();
                r && clearTimeout(r), !i && f.width() && (r = setTimeout(function () {
                    var i = y ? e.height() : l(window).height(),
                        n = y ? e.prop("scrollHeight") : document.documentElement.scrollHeight;
                    n - t - i <= u && (o || g())
                }, 100))
            }), a) : a
        }
    }, o.prototype.lazyimg = function (e) {
        var o, t = this, i = 0;
        e = e || {};
        var n = l(e.scrollElem || document), r = e.elem || "img", a = e.scrollElem && e.scrollElem !== document,
            c = function (e, l) {
                var o = n.scrollTop(), r = o + l, c = a ? function () {
                    return e.offset().top - n.offset().top + o
                }() : e.offset().top;
                if (c >= o && c <= r && !e.attr("src")) {
                    var m = e.attr("lay-src");
                    layui.img(m, function () {
                        var l = t.lazyimg.elem.eq(i);
                        e.attr("src", m).removeAttr("lay-src"), l[0] && f(l), i++
                    })
                }
            }, f = function (e, o) {
                var f = a ? (o || n).height() : l(window).height(), m = n.scrollTop(), u = m + f;
                if (t.lazyimg.elem = l(r), e) c(e, f); else for (var s = 0; s < t.lazyimg.elem.length; s++) {
                    var v = t.lazyimg.elem.eq(s), y = a ? function () {
                        return v.offset().top - n.offset().top + m
                    }() : v.offset().top;
                    if (c(v, f), i = s, y > u) break
                }
            };
        if (f(), !o) {
            var m;
            n.on("scroll", function () {
                var e = l(this);
                m && clearTimeout(m), m = setTimeout(function () {
                    f(null, e)
                }, 50)
            }), o = !0
        }
        return f
    }, e("flow", new o)
});
layui.define(["layer", "form"], function (t) {
    "use strict";
    var e = layui.$, i = layui.layer, a = layui.form, l = (layui.hint(), layui.device()), n = "layedit",
        o = "layui-show", r = "layui-disabled", c = function () {
            var t = this;
            t.index = 0, t.config = {
                tool: ["strong", "italic", "underline", "del", "|", "left", "center", "right", "|", "link", "unlink", "face", "image"],
                hideTool: [],
                height: 280
            }
        };
    c.prototype.set = function (t) {
        var i = this;
        return e.extend(!0, i.config, t), i
    }, c.prototype.on = function (t, e) {
        return layui.onevent(n, t, e)
    }, c.prototype.build = function (t, i) {
        i = i || {};
        var a = this, n = a.config, r = "layui-layedit", c = e("string" == typeof t ? "#" + t : t),
            u = "LAY_layedit_" + ++a.index, d = c.next("." + r), y = e.extend({}, n, i), f = function () {
                var t = [], e = {};
                return layui.each(y.hideTool, function (t, i) {
                    e[i] = !0
                }), layui.each(y.tool, function (i, a) {
                    C[a] && !e[a] && t.push(C[a])
                }), t.join("")
            }(),
            m = e(['<div class="' + r + '">', '<div class="layui-unselect layui-layedit-tool">' + f + "</div>", '<div class="layui-layedit-iframe">', '<iframe id="' + u + '" name="' + u + '" textarea="' + t + '" frameborder="0"></iframe>', "</div>", "</div>"].join(""));
        return l.ie && l.ie < 8 ? c.removeClass("layui-hide").addClass(o) : (d[0] && d.remove(), s.call(a, m, c[0], y), c.addClass("layui-hide").after(m), a.index)
    }, c.prototype.getContent = function (t) {
        var e = u(t);
        if (e[0]) return d(e[0].document.body.innerHTML)
    }, c.prototype.getText = function (t) {
        var i = u(t);
        if (i[0]) return e(i[0].document.body).text()
    }, c.prototype.setContent = function (t, i, a) {
        var l = u(t);
        l[0] && (a ? e(l[0].document.body).append(i) : e(l[0].document.body).html(i), layedit.sync(t))
    }, c.prototype.sync = function (t) {
        var i = u(t);
        if (i[0]) {
            var a = e("#" + i[1].attr("textarea"));
            a.val(d(i[0].document.body.innerHTML))
        }
    }, c.prototype.getSelection = function (t) {
        var e = u(t);
        if (e[0]) {
            var i = m(e[0].document);
            return document.selection ? i.text : i.toString()
        }
    };
    var s = function (t, i, a) {
        var l = this, n = t.find("iframe");
        n.css({height: a.height}).on("load", function () {
            var o = n.contents(), r = n.prop("contentWindow"), c = o.find("head"),
                s = e(["<style>", "*{margin: 0; padding: 0;}", "body{padding: 10px; line-height: 20px; overflow-x: hidden; word-wrap: break-word; font: 14px Helvetica Neue,Helvetica,PingFang SC,Microsoft YaHei,Tahoma,Arial,sans-serif; -webkit-box-sizing: border-box !important; -moz-box-sizing: border-box !important; box-sizing: border-box !important;}", "a{color:#01AAED; text-decoration:none;}a:hover{color:#c00}", "p{margin-bottom: 10px;}", "img{display: inline-block; border: none; vertical-align: middle;}", "pre{margin: 10px 0; padding: 10px; line-height: 20px; border: 1px solid #ddd; border-left-width: 6px; background-color: #F2F2F2; color: #333; font-family: Courier New; font-size: 12px;}", "</style>"].join("")),
                u = o.find("body");
            c.append(s), u.attr("contenteditable", "true").css({"min-height": a.height}).html(i.value || ""), y.apply(l, [r, n, i, a]), g.call(l, r, t, a)
        })
    }, u = function (t) {
        var i = e("#LAY_layedit_" + t), a = i.prop("contentWindow");
        return [a, i]
    }, d = function (t) {
        return 8 == l.ie && (t = t.replace(/<.+>/g, function (t) {
            return t.toLowerCase()
        })), t
    }, y = function (t, a, n, o) {
        var r = t.document, c = e(r.body);
        c.on("keydown", function (t) {
            var e = t.keyCode;
            if (13 === e) {
                var a = m(r), l = p(a), n = l.parentNode;
                if ("pre" === n.tagName.toLowerCase()) {
                    if (t.shiftKey) return;
                    return i.msg("请暂时用shift+enter"), !1
                }
                r.execCommand("formatBlock", !1, "<p>")
            }
        }), e(n).parents("form").on("submit", function () {
            var t = c.html();
            8 == l.ie && (t = t.replace(/<.+>/g, function (t) {
                return t.toLowerCase()
            })), n.value = t
        }), c.on("paste", function (e) {
            r.execCommand("formatBlock", !1, "<p>"), setTimeout(function () {
                f.call(t, c), n.value = c.html()
            }, 100)
        })
    }, f = function (t) {
        var i = this;
        i.document;
        t.find("*[style]").each(function () {
            var t = this.style.textAlign;
            this.removeAttribute("style"), e(this).css({"text-align": t || ""})
        }), t.find("table").addClass("layui-table"), t.find("script,link").remove()
    }, m = function (t) {
        return t.selection ? t.selection.createRange() : t.getSelection().getRangeAt(0)
    }, p = function (t) {
        return t.endContainer || t.parentElement().childNodes[0]
    }, v = function (t, i, a) {
        var l = this.document, n = document.createElement(t);
        for (var o in i) n.setAttribute(o, i[o]);
        if (n.removeAttribute("text"), l.selection) {
            var r = a.text || i.text;
            if ("a" === t && !r) return;
            r && (n.innerHTML = r), a.pasteHTML(e(n).prop("outerHTML")), a.select()
        } else {
            var r = a.toString() || i.text;
            if ("a" === t && !r) return;
            r && (n.innerHTML = r), a.deleteContents(), a.insertNode(n)
        }
    }, h = function (t, i) {
        var a = this.document, l = "layedit-tool-active", n = p(m(a)), o = function (e) {
            return t.find(".layedit-tool-" + e)
        };
        i && i[i.hasClass(l) ? "removeClass" : "addClass"](l), t.find(">i").removeClass(l), o("unlink").addClass(r), e(n).parents().each(function () {
            var t = this.tagName.toLowerCase(), e = this.style.textAlign;
            "b" !== t && "strong" !== t || o("b").addClass(l), "i" !== t && "em" !== t || o("i").addClass(l), "u" === t && o("u").addClass(l), "strike" === t && o("d").addClass(l), "p" === t && ("center" === e ? o("center").addClass(l) : "right" === e ? o("right").addClass(l) : o("left").addClass(l)), "a" === t && (o("link").addClass(l), o("unlink").removeClass(r))
        })
    }, g = function (t, a, l) {
        var n = t.document, o = e(n.body), c = {
            link: function (i) {
                var a = p(i), l = e(a).parent();
                b.call(o, {href: l.attr("href"), target: l.attr("target")}, function (e) {
                    var a = l[0];
                    "A" === a.tagName ? a.href = e.url : v.call(t, "a", {target: e.target, href: e.url, text: e.url}, i)
                })
            }, unlink: function (t) {
                n.execCommand("unlink")
            }, face: function (e) {
                x.call(this, function (i) {
                    v.call(t, "img", {src: i.src, alt: i.alt}, e)
                })
            }, image: function (a) {
                var n = this;
                layui.use("upload", function (o) {
                    var r = l.uploadImage || {};
                    o.render({
                        url: r.url, method: r.type, elem: e(n).find("input")[0], done: function (e) {
                            0 == e.code ? (e.data = e.data || {}, v.call(t, "img", {
                                src: e.data.src,
                                alt: e.data.title
                            }, a)) : i.msg(e.msg || "上传失败")
                        }
                    })
                })
            }, code: function (e) {
                k.call(o, function (i) {
                    v.call(t, "pre", {text: i.code, "lay-lang": i.lang}, e)
                })
            }, help: function () {
                i.open({
                    type: 2,
                    title: "帮助",
                    area: ["600px", "380px"],
                    shadeClose: !0,
                    shade: .1,
                    skin: "layui-layer-msg",
                    content: ["", "no"]
                })
            }
        }, s = a.find(".layui-layedit-tool"), u = function () {
            var i = e(this), a = i.attr("layedit-event"), l = i.attr("lay-command");
            if (!i.hasClass(r)) {
                o.focus();
                var u = m(n);
                u.commonAncestorContainer;
                l ? (n.execCommand(l), /justifyLeft|justifyCenter|justifyRight/.test(l) && n.execCommand("formatBlock", !1, "<p>"), setTimeout(function () {
                    o.focus()
                }, 10)) : c[a] && c[a].call(this, u), h.call(t, s, i)
            }
        }, d = /image/;
        s.find(">i").on("mousedown", function () {
            var t = e(this), i = t.attr("layedit-event");
            d.test(i) || u.call(this)
        }).on("click", function () {
            var t = e(this), i = t.attr("layedit-event");
            d.test(i) && u.call(this)
        }), o.on("click", function () {
            h.call(t, s), i.close(x.index)
        })
    }, b = function (t, e) {
        var l = this, n = i.open({
            type: 1,
            id: "LAY_layedit_link",
            area: "350px",
            shade: .05,
            shadeClose: !0,
            moveType: 1,
            title: "超链接",
            skin: "layui-layer-msg",
            content: ['<ul class="layui-form" style="margin: 15px;">', '<li class="layui-form-item">', '<label class="layui-form-label" style="width: 60px;">URL</label>', '<div class="layui-input-block" style="margin-left: 90px">', '<input name="url" lay-verify="url" value="' + (t.href || "") + '" autofocus="true" autocomplete="off" class="layui-input">', "</div>", "</li>", '<li class="layui-form-item">', '<label class="layui-form-label" style="width: 60px;">打开方式</label>', '<div class="layui-input-block" style="margin-left: 90px">', '<input type="radio" name="target" value="_self" class="layui-input" title="当前窗口"' + ("_self" !== t.target && t.target ? "" : "checked") + ">", '<input type="radio" name="target" value="_blank" class="layui-input" title="新窗口" ' + ("_blank" === t.target ? "checked" : "") + ">", "</div>", "</li>", '<li class="layui-form-item" style="text-align: center;">', '<button type="button" lay-submit lay-filter="layedit-link-yes" class="layui-btn"> 确定 </button>', '<button style="margin-left: 20px;" type="button" class="layui-btn layui-btn-primary"> 取消 </button>', "</li>", "</ul>"].join(""),
            success: function (t, n) {
                var o = "submit(layedit-link-yes)";
                a.render("radio"), t.find(".layui-btn-primary").on("click", function () {
                    i.close(n), l.focus()
                }), a.on(o, function (t) {
                    i.close(b.index), e && e(t.field)
                })
            }
        });
        b.index = n
    }, x = function (t) {
        var a = function () {
            var t = ["[微笑]", "[嘻嘻]", "[哈哈]", "[可爱]", "[可怜]", "[挖鼻]", "[吃惊]", "[害羞]", "[挤眼]", "[闭嘴]", "[鄙视]", "[爱你]", "[泪]", "[偷笑]", "[亲亲]", "[生病]", "[太开心]", "[白眼]", "[右哼哼]", "[左哼哼]", "[嘘]", "[衰]", "[委屈]", "[吐]", "[哈欠]", "[抱抱]", "[怒]", "[疑问]", "[馋嘴]", "[拜拜]", "[思考]", "[汗]", "[困]", "[睡]", "[钱]", "[失望]", "[酷]", "[色]", "[哼]", "[鼓掌]", "[晕]", "[悲伤]", "[抓狂]", "[黑线]", "[阴险]", "[怒骂]", "[互粉]", "[心]", "[伤心]", "[猪头]", "[熊猫]", "[兔子]", "[ok]", "[耶]", "[good]", "[NO]", "[赞]", "[来]", "[弱]", "[草泥马]", "[神马]", "[囧]", "[浮云]", "[给力]", "[围观]", "[威武]", "[奥特曼]", "[礼物]", "[钟]", "[话筒]", "[蜡烛]", "[蛋糕]"],
                e = {};
            return layui.each(t, function (t, i) {
                e[i] = layui.cache.dir + "images/face/" + t + ".gif"
            }), e
        }();
        return x.hide = x.hide || function (t) {
            "face" !== e(t.target).attr("layedit-event") && i.close(x.index)
        }, x.index = i.tips(function () {
            var t = [];
            return layui.each(a, function (e, i) {
                t.push('<li title="' + e + '"><img src="' + i + '" alt="' + e + '"></li>')
            }), '<ul class="layui-clear">' + t.join("") + "</ul>"
        }(), this, {
            tips: 1, time: 0, skin: "layui-box layui-util-face", maxWidth: 500, success: function (l, n) {
                l.css({marginTop: -4, marginLeft: -10}).find(".layui-clear>li").on("click", function () {
                    t && t({src: a[this.title], alt: this.title}), i.close(n)
                }), e(document).off("click", x.hide).on("click", x.hide)
            }
        })
    }, k = function (t) {
        var e = this, l = i.open({
            type: 1,
            id: "LAY_layedit_code",
            area: "550px",
            shade: .05,
            shadeClose: !0,
            moveType: 1,
            title: "插入代码",
            skin: "layui-layer-msg",
            content: ['<ul class="layui-form layui-form-pane" style="margin: 15px;">', '<li class="layui-form-item">', '<label class="layui-form-label">请选择语言</label>', '<div class="layui-input-block">', '<select name="lang">', '<option value="JavaScript">JavaScript</option>', '<option value="HTML">HTML</option>', '<option value="CSS">CSS</option>', '<option value="Java">Java</option>', '<option value="PHP">PHP</option>', '<option value="C#">C#</option>', '<option value="Python">Python</option>', '<option value="Ruby">Ruby</option>', '<option value="Go">Go</option>', "</select>", "</div>", "</li>", '<li class="layui-form-item layui-form-text">', '<label class="layui-form-label">代码</label>', '<div class="layui-input-block">', '<textarea name="code" lay-verify="required" autofocus="true" class="layui-textarea" style="height: 200px;"></textarea>', "</div>", "</li>", '<li class="layui-form-item" style="text-align: center;">', '<button type="button" lay-submit lay-filter="layedit-code-yes" class="layui-btn"> 确定 </button>', '<button style="margin-left: 20px;" type="button" class="layui-btn layui-btn-primary"> 取消 </button>', "</li>", "</ul>"].join(""),
            success: function (l, n) {
                var o = "submit(layedit-code-yes)";
                a.render("select"), l.find(".layui-btn-primary").on("click", function () {
                    i.close(n), e.focus()
                }), a.on(o, function (e) {
                    i.close(k.index), t && t(e.field)
                })
            }
        });
        k.index = l
    }, C = {
        html: '<i class="layui-icon layedit-tool-html" title="HTML源代码" lay-command="html" layedit-event="html"">&#xe64b;</i><span class="layedit-tool-mid"></span>',
        strong: '<i class="layui-icon layedit-tool-b" title="加粗" lay-command="Bold" layedit-event="b"">&#xe62b;</i>',
        italic: '<i class="layui-icon layedit-tool-i" title="斜体" lay-command="italic" layedit-event="i"">&#xe644;</i>',
        underline: '<i class="layui-icon layedit-tool-u" title="下划线" lay-command="underline" layedit-event="u"">&#xe646;</i>',
        del: '<i class="layui-icon layedit-tool-d" title="删除线" lay-command="strikeThrough" layedit-event="d"">&#xe64f;</i>',
        "|": '<span class="layedit-tool-mid"></span>',
        left: '<i class="layui-icon layedit-tool-left" title="左对齐" lay-command="justifyLeft" layedit-event="left"">&#xe649;</i>',
        center: '<i class="layui-icon layedit-tool-center" title="居中对齐" lay-command="justifyCenter" layedit-event="center"">&#xe647;</i>',
        right: '<i class="layui-icon layedit-tool-right" title="右对齐" lay-command="justifyRight" layedit-event="right"">&#xe648;</i>',
        link: '<i class="layui-icon layedit-tool-link" title="插入链接" layedit-event="link"">&#xe64c;</i>',
        unlink: '<i class="layui-icon layedit-tool-unlink layui-disabled" title="清除链接" lay-command="unlink" layedit-event="unlink"">&#xe64d;</i>',
        face: '<i class="layui-icon layedit-tool-face" title="表情" layedit-event="face"">&#xe650;</i>',
        image: '<i class="layui-icon layedit-tool-image" title="图片" layedit-event="image">&#xe64a;<input type="file" name="file"></i>',
        code: '<i class="layui-icon layedit-tool-code" title="插入代码" layedit-event="code">&#xe64e;</i>',
        help: '<i class="layui-icon layedit-tool-help" title="帮助" layedit-event="help">&#xe607;</i>'
    }, L = new c;
    t(n, L)
});
// layui.define("jquery", function (a) {
//     "use strict";
//     var e = layui.$;
//     a("code", function (a) {
//         var l = [];
//         a = a || {}, a.elem = e(a.elem || ".layui-code"), a.lang = "lang" in a ? a.lang : "code", a.elem.each(function () {
//             l.push(this)
//         }), layui.each(l.reverse(), function (l, i) {
//             var t = e(i), c = t.html();
//             (t.attr("lay-encode") || a.encode) && (c = c.replace(/&(?!#?[a-zA-Z0-9]+;)/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#39;").replace(/"/g, "&quot;")), t.html('<ol class="layui-code-ol"><li>' + c.replace(/[\r\t\n]+/g, "</li><li>") + "</li></ol>"), t.find(">.layui-code-h3")[0] || t.prepend('<h3 class="layui-code-h3">' + (t.attr("lay-title") || a.title || "&lt;/&gt;") + '<a href="javascript:;">' + (t.attr("lay-lang") || a.lang || "") + "</a></h3>");
//             var n = t.find(">.layui-code-ol");
//             t.addClass("layui-box layui-code-view"), (t.attr("lay-skin") || a.skin) && t.addClass("layui-code-" + (t.attr("lay-skin") || a.skin)), (n.find("li").length / 100 | 0) > 0 && n.css("margin-left", (n.find("li").length / 100 | 0) + "px"), (t.attr("lay-height") || a.height) && n.css("max-height", t.attr("lay-height") || a.height)
//         })
//     })
// }).addcss("modules/code.css?v=2", "skincodecss");
layui.define("jquery", function (a) {
    "use strict";
    var e = layui.$;
    a("code", function (a) {
        var l = [];
        a = a || {}, a.elem = e(a.elem || ".layui-code"), a.lang = "lang" in a ? a.lang : "code", a.elem.each(function () {
            l.push(this)
        }), layui.each(l.reverse(), function (l, i) {
            var t = e(i), c = t.html();
            (t.attr("lay-encode") || a.encode) && (c = c.replace(/&(?!#?[a-zA-Z0-9]+;)/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#39;").replace(/"/g, "&quot;")), t.html('<ol class="layui-code-ol"><li>' + c.replace(/[\r\t\n]+/g, "</li><li>") + "</li></ol>"), t.find(">.layui-code-h3")[0] || t.prepend('<h3 class="layui-code-h3">' + (t.attr("lay-title") || a.title || "&lt;/&gt;") + '<a href="javascript:;">' + (t.attr("lay-lang") || a.lang || "") + "</a></h3>");
            var n = t.find(">.layui-code-ol");
            t.addClass("layui-box layui-code-view"), (t.attr("lay-skin") || a.skin) && t.addClass("layui-code-" + (t.attr("lay-skin") || a.skin)), (n.find("li").length / 100 | 0) > 0 && n.css("margin-left", (n.find("li").length / 100 | 0) + "px"), (t.attr("lay-height") || a.height) && n.css("max-height", t.attr("lay-height") || a.height)
        })
    })
});