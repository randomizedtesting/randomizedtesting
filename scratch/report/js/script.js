(function($) {
  // Constants
  var OK = "OK";
  var FAILURE = "FAILURE";
  var ERROR = "ERROR";
  var IGNORED = "IGNORED";
  var IGNORED_ASSUMPTION = "IGNORED_ASSUMPTION";
  
  var statusOrder = [ OK, IGNORED_ASSUMPTION, IGNORED, ERROR, FAILURE ];
  var statusLabels = {
    OK: "OK",
    FAILURE: "FAIL",
    ERROR: "ERROR",
    IGNORED: "IGNORED",
    IGNORED_ASSUMPTION: "IGNORED"
  };

  var $table, $tools;
  var data = suites, aggregates;
  var currentView, currentOrder, currentSearch = "";

  // Table definitions
  var tables = (function() {
    // Common columns of the aggregated view tables 
    var aggregatedViewColumns = [
      column("signature", "string", "", true),
      numericColumn("count", "Tests"),
      {
        id: "result",
        label: "Result",
        sortable: true,
        sorting: function(a, b) {
          for (var i = 0; i < statusOrder.length; i++) {
            var s = statusOrder[statusOrder.length - i];
            if ((a.statuses[s] || 0) != (b.statuses[s] || 0)) {
              return (b.statuses[s] || 0) - (a.statuses[s] || 0);
            }
          }
          return 0;
        },
        renderer: function(value, html) {
          html.push(statusbar(value.statuses, value.total));
        },
        type: "result"
      },
      numericColumn("pass", "Pass"),
      numericColumn("ignored", "Ign"),
      numericColumn("error", "Err"),
      numericColumn("failed", "Fail"),
      numericColumn("time", "Time [ms]")
    ];

    return {
      byMethod: {
        columns: [
          column("signature", "string", "Method", true),
          {
            id: "result",
            label: "Result",
            sortable: true,
            sorting: function(a, b) {
              if (a == b) {
                return 0;
              } else {
                for (var i = 0; i < statusOrder.length; i++) {
                  var status = statusOrder[i];
                  if (a == status) {
                    return -1;
                  }
                  if (b == status) {
                    return 1;
                  }
                }
              }
            },
            renderer: function(value, html) {
              html.push("<span class='", value, "'>", statusLabels[value] ,"</span>")
            },
            type: "result"
          },
          numericColumn("slave", "JVM"),
          {
            id: "timestamp",
            label: "Start",
            type: "numeric",
            renderer: function(value, html) {
              var d = new Date(value);
              html.push(zero(d.getHours()), ":", zero(d.getMinutes()), ":", zero(d.getSeconds()), ".", zerozero(d.getMilliseconds()));

              function zero(x) {
                if (x < 10) {
                  return "0" + x;
                } else {
                  return x;
                }
              }
              function zerozero(x) {
                if (x < 100) {
                  return "0" + zero(x);
                } else {
                  return x;
                }
              }
            },
            sortable: true
          },
          numericColumn("time", "Time [ms]")
        ],
        rows: function(data, aggregates) {
          var rows  = [];
          eachTest(data, function(test) {
            if (!currentFilter(test)) {
              return;
            }
            rows.push({
              signature: test.description.packageClassMethodName,
              result: test.status,
              time: test.executionTime,
              slave: test.slave,
              timestamp: test.startTimestamp
            })
          });
          return rows;
        }
      },

      byPackage: {
        columns: $.extend(true, [], aggregatedViewColumns, [ { label: "Package" } ]),
        rows: function(data) {
          return aggregatedRows(data, byPackage);
        }
      },

      byClass: {
        columns: $.extend(true, [], aggregatedViewColumns, [ { label: "Class" } ]),
        rows: function(data) {
          return aggregatedRows(data, byClass);
        }
      }
    };

    function column(id, type, label, searchable) {
      return {
        id: id,
        sortable: true,
        searchable: searchable,
        type: type,
        label: label,
        renderer: function(value, html) {
          if (this.searchable && currentSearch) {
            var s = currentSearch.toLowerCase(), sl = s.length, vlc = value.toLowerCase(), vl = value.length;
            var start = 0, found = -1;
            while ((found = vlc.indexOf(s, start)) >= 0) {
              html.push(value.substring(start, found), "<em>", value.substring(found, found + sl), "</em>");
              start = found + 1;
            }
            html.push(value.substring(start + sl - 1));
          } else {
            html.push(escape(value));
          }
        }
      };
    }

    function numericColumn(id, label) {
      return column(id, "numeric", label, false);
    }

    function aggregatedRows(data, aggregateFunction) {
      var rows = [];

      var counts = aggregate(data, testCount, { aggregate: aggregateFunction }, currentFilter);
      var times = aggregate(data, totalTime, { aggregate: aggregateFunction }, currentFilter);
      var statuses = aggregate(data, testCountByStatus, { aggregate: aggregateFunction }, currentFilter);

      if (!statuses.aggregate) {
        return rows;
      }
      $.each(statuses.aggregate, function(signature, statuses) {
        rows.push({
          signature: signature,
          count: counts.aggregate[signature],
          result: { statuses: statuses, total: counts.aggregate[signature] },
          pass: statuses[OK] || 0,
          ignored: (statuses[IGNORED] || 0) + (statuses[IGNORED_ASSUMPTION] || 0),
          error: statuses[ERROR] || 0,
          failed: statuses[FAILURE] || 0,
          time: times.aggregate[signature]
        });
      });
      return rows;
    }
  })();

  // Initialize the table
  $(document).ready(function() {

    // Global preprocessing of the data:
    // Split method names into semantic parts
    eachTest(data, function (test) {
      var description = test.description;

      description.packageClassName = description.className;

      var methodSplit = description.methodName.split(" ");
      description.methodName = methodSplit[0];
      description.methodExtras = methodSplit[1];

      description.packageClassMethodName = description.className + "." + description.methodName;

      var classSplit = description.className.split("\.");
      description.className = classSplit.pop();
      description.packageName = classSplit.join(".");
    });

    // Create global aggregations
    var counts = aggregate(data, testCount, { "global":global, "byStatus":byStatus }, noFilter);

    // Generate markup
    var $results = $("#results");

    // Results heading
    var heading = { };
    if (counts.byStatus[FAILURE] > 0) {
      heading.text = countText(counts.byStatus[FAILURE], "test") + " failed";
      heading.class = FAILURE;
    } else if (counts.byStatus[ERROR] > 0) {
      heading.text = countText(counts.byStatus[ERROR], "test") + " had errors";
      heading.class = ERROR;
    } else {
      heading.text = "tests successful";
      heading.class = OK;
    }
    $("header > h1").append(" <strong>" + heading.text + "</strong>").parent().addClass(heading.class);

    // Update window title
    document.title = $.trim($("header > h1").text());

    // Results table tools
    $tools = $("<div id='tools'>\
      <input type='search' accesskey='s' placeholder='package, class, method name (Alt+Shift+S to focus)' />\
      view: <a href='#packages'>packages</a> <a href='#classes'>classes</a> <a href='#methods'>methods</a>\
    </div>").appendTo($results);


    // Results table
    $table = $("<table />").appendTo($results);

    // Bind listeners
    $tools.on("click", "a", function () {
      currentView = $(this).attr("href").substring(1);
      refreshTable();
      return false;
    });

    $table.on("click", "th.sortable", function (e) {
      var newSort = $(this).data("column");
      if (e.ctrlKey) {
        // If the ordering already contains the selected column, invert
        // the order. If the ordering does not contain the selected column,
        // add it at the end of the ordering array.
        var matched = false;
        for (var i = 0; i < currentOrder.columns.length; i++) {
          if (currentOrder.columns[i] == newSort) {
            currentOrder.ascendings[i] = !currentOrder.ascendings[i];
            matched = true;
            break;
          }
        }
        if (!matched) {
          currentOrder.columns.push(newSort);
          currentOrder.ascendings.push(true);
        }
      } else {
        // Sort by just by the requested column or change sorting order
        var currentAscending = false;
        for (var i = 0; i < currentOrder.columns.length; i++) {
          if (currentOrder.columns[i] == newSort) {
            currentAscending = currentOrder.ascendings[i];
            break;
          }
        }
        currentOrder.columns = [ newSort ];
        currentOrder.ascendings = [ !currentAscending ];
      }
      refreshTable();
      return false;
    });

    $tools.find("input[type='search']").on("keyup click", function() {
      var $this = $(this);
      typewatch(function() {
        var v = $.trim($this.val());
        if (currentSearch != v) {
          currentSearch = v;
          refresh();
        }
      }, 500);
    });

    // If no failures or errors, show package view ordered by package name.
    // In case of errors or failures, show method view ordered by status.
    if (!(counts.byStatus[FAILURE] > 0 || counts.byStatus[ERROR] > 0)) {
      currentView = "packages";
      currentOrder = { columns:[ "signature" ], ascendings:[ true ] };
    } else {
      currentView = "methods";
      currentOrder = { columns:[ "result" ], ascendings:[ false ] };
    }

    refresh();
    return this;
  });

  function refresh() {
    refreshTable();
    refreshSummary();
  }

  // Refreshes the results table based on the current parameters
  function refreshTable() {
    switch (currentView) {
      case "packages":
        $table.html(table(tables.byPackage, data, currentOrder)).attr("class", "package");
        break;

      case "classes":
        $table.html(table(tables.byClass, data, currentOrder)).attr("class", "class");
        break;

      case "methods":
        $table.html(table(tables.byMethod, data, currentOrder)).attr("class", "method");
        break;
    }
    $tools.find("a").removeClass("active").filter("[href^=#" + currentView + "]").addClass("active");
  }

  // Refreshes the summary box based on the current parameters
  function refreshSummary() {
    var counts = aggregate(data, testCount, { "global":global, "byStatus":byStatus }, currentFilter);
    var times = aggregate(data, totalTime, { "global":global, "bySlave":bySlave }, currentFilter);

    var $summary = $("#summary").html("").attr("class", "");
    if ((counts.global || 0) == 0) {
      return;
    }

    $("<p />").html(tmpl("\
        #{tests} executed in\
        #{time} ms on\
        <a href='#'>#{slaves}</a>.", {
      tests: countText(counts.global || 0, "test"),
      time: times.global,
      slaves: countText(keys(times.bySlave).length, "slave")
    })).appendTo($summary);

    var html = "";
    if (!(counts.byStatus[FAILURE] > 0 || counts.byStatus[ERROR] > 0)) {
      if (counts.byStatus[OK] == counts.global) {
        html = "All tests passed.";
      } else if ((counts.byStatus[OK] || 0) > 0) {
        html = tmpl("No failures, #{passed} passed, #{ignored} ignored.", {
          passed:countText(counts.byStatus[OK], "test"),
          ignored:countText((counts.byStatus[IGNORED] || 0) + (counts.byStatus[IGNORED_ASSUMPTION] || 0), "test")
        });
      }
    } else {
      var h = [];
      if (counts.byStatus[FAILURE] > 0) {
        h.push(counts.byStatus[FAILURE] + " failed");
      }
      if (counts.byStatus[ERROR] > 0) {
        h.push(countText(counts.byStatus[ERROR], "error"));
      }
      if (counts.byStatus[IGNORED] > 0 || counts.byStatus[IGNORED_ASSUMPTION] > 0) {
        h.push((counts.byStatus[IGNORED] || 0) + (counts.byStatus[IGNORED_ASSUMPTION] || 0) + " ignored");
      }
      if (counts.byStatus[OK] > 0) {
        h.push(counts.byStatus[OK] + " passed");
      }
      html = h.join(", ");
    }
    $("<p />").html(html).appendTo($summary);

    // Status bar
    $summary.append($(statusbar(counts.byStatus, counts.global)));
  }

  // Renders contents of a table according to the provided spec
  function table(spec, data, order) {
    var html = [ ];

    // Get the data
    var rows = spec.rows(data);
    if (rows.length == 0) {
      html.push("<thead class='empty'><tr><th>", "No tests results found" ,"</th></tr></thead>");
      return html.join("");
    }

    var allColumnsById = map(spec.columns, function(c) { return c.id; });
    var orderColumns = [ ];
    for (var i = 0; i < order.columns.length; i++) {
      orderColumns[i] = {
        column: allColumnsById[order.columns[i]] || spec.columns[0],
        ascending: order.ascendings[i]
      };
    }
    var orderColumnsById = map (orderColumns, function(o) { return o.column.id; });

    // Render column headers
    html.push("<thead>");
    html.push("<tr>");
    $.each(spec.columns, function(i, column) {
      html.push(tmpl("<th class='#{type} #{id} #{sort} #{sortable}' data-column='#{id}'><span>#{label}</span></th>", {
        type: column.type,
        id: column.id,
        sort: typeof orderColumnsById[column.id] != 'undefined' ? (orderColumnsById[column.id].ascending ? "asc" : "desc") : "",
        label: column.label,
        sortable: column.sortable ? "sortable" : ""
      }));
    });
    html.push("</tr>");
    html.push("</thead>");

    // Sort the data
    rows.sort(function(a, b) {
      for (var i = 0; i < orderColumns.length; i++) {
        var column = orderColumns[i].column;
        var ordering = column.sorting || function(a, b) { return a > b ? 1 : b > a ? -1 : 0; }
        var o = ordering(a[column.id], b[column.id]) * (orderColumns[i].ascending ? 1 : -1);
        if (o != 0) {
          return o;
        }
      }
      return 0;
    });

    // Render table rows
    html.push("<tbody>");
    $.each(rows, function(i, row) {
      html.push("<tr>");
      $.each(spec.columns, function(i, column) {
        html.push("<td class='", column.type, " ", column.id, "'>");
        column.renderer(row[column.id], html);
        html.push("</td>");
      });
      html.push("</tr>");
    });
    html.push("</tbody>");

    return html.join("");
  }

  function time(id, code) {
    var start = Date.now();
    code();
    console.log(id, Date.now() - start);
  }

  function statusbar(counts, total) {
    var html = [];
    html.push("<ul class='statusbar'>");
    var left = 0;
    for (var i = 0; i < statusOrder.length; i++) {
      var status = statusOrder[i];
      var count = counts[status];
      if (count > 0) {
        var width = (100 * count) / total;
        html.push(tmpl("<li class='#{status}' style='width: #{pct}%; left: #{left}%' title='#{count} #{label}'></li>", {
          status: status,
          label: statusLabels[status],
          pct: width,
          count: count,
          left: left
        }));
        left += width;
      }
    }
    html.push("</ul>");
    return html.join("");
  };
  
  /**
   * Aggregates the data using the provided aggregation function
   * and the provided set of key transformer functions.
   */
  function aggregate(data, aggregation, keys, filter) {
    var a = { };
    eachTest(data, function(test) {
      if (!filter(test)) {
        return true;
      }

      $.each(keys, function(id, key) {
        var k = key.call(this, test);

        // If undefined, treat as global key and don't create an extra holder object
        if (k === undefined) {
          a[id] = aggregation.call(this, test, a[id]);
        } else {
          a[id] = a[id] || { };
          a[id][k] = aggregation.call(this, test, a[id][k]);
        }
      });
    });
    return a;
  }

  function byStatus(test) {
    return test.status;
  }

  function bySlave(test) {
    return test.slave;
  }
  
  function byPackage(test) {
    return test.description.packageName;
  }

  function byClass(test) {
    return test.description.packageClassName;
  }

  function noFilter(test) {
    return true;
  }

  var searchTargetsByView = { packages: "packageName", classes: "packageClassName", methods: "packageClassMethodName" };
  function signatureSearchFilter(test) {
    if ($.trim(currentSearch).length > 0) {
      return test.description[searchTargetsByView[currentView]].toLowerCase().indexOf(currentSearch.toLowerCase()) >= 0;
    } else {
      return true;
    }
  }

  function currentFilter(test) {
    return signatureSearchFilter(test);
  }

  function global() {
    return undefined;
  }

  function testCountByStatus(test, current) {
    return addOne(current || { }, test.status);
  }

  function testCount(test, current) {
    return (current || 0) + 1;
  }

  function totalTime(test, current) {
    return (current || 0) + test.executionTime;
  }

  function getOrCreate(set, key, created) {
    if (typeof set[key] === "undefined") {
      set[key] = created;
    }
    return set[key];
  }

  function addOne(set, key) {
    if (typeof set[key] == "undefined") {
      set[key] = 1;
    } else {
      set[key]++;
    }
    return set;
  }

  function keys(o) {
    var a = [ ];
    for (k in o) {
      if (o.hasOwnProperty(k)) {
        a.push(k);
      }
    }
    return a;
  }

  function map(collection, key) {
    var result = { };
    $.each(collection, function(i, value) {
      result[key(value)] = value;
    });
    return result;
  }

  function countText(count, noun) {
    return count + " " + noun + (count == 1 ? "" : "s");
  }

  function eachTest(data, callback) {
    for (var i = 0; i < data.length; i++) {
      var tests = data[i].tests;
      for (var j = 0; j < tests.length; j++) {
        callback.call(this, tests[j]);
      }
    }
  }


  // Inspired by jQuery Simple Templates plugin 1.1.1
  // http://andrew.hedges.name/tmpl
  // Dual licensed under the MIT and GPL licenses
  function tmpl(tmpl, vals) {
    var rgxp, repr;
      
    tmpl = tmpl  || '';
    vals = vals || {};
      
    // regular expression for matching placeholders
    rgxp = /#\{([^{}]*)}/g;
   
    // make replacements
    repr = function (str, match) {
      var m = match.split(":");
      var v = m[0];
      var t = m[1];
      return typeof vals[v] === 'string' && t != "raw" ? e(vals[v]) : vals[v];
      
      function e(content) {
        return content ? escape(content) : content;
      }
    };
    
    return tmpl.replace(rgxp, repr);
  }
  
  function escape(string) {
    return typeof string === 'string' ? string.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/&/g, "&amp;") : string;
  }

  var typewatch = (function(){
    var timer = 0;
    return function(callback, ms){
      clearTimeout (timer);
      timer = setTimeout(callback, ms);
    }
  })();
})(jQuery);
