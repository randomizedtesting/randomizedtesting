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

  // Table definitions
  var tables = (function() {
    // Common columns of the aggregated view tables 
    var aggregatedViewColumns = [
      column("signature", "string", ""),
      numericColumn("count", "Tests"),
     
      {
        id: "result",
        label: "Result",
        sortable: true,
        sorting: function(a, b) {
          for (var i = 0; i < statusOrder.length; i++) {
            var s = statusOrder[i];
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
      byPackage: {
        columns: $.extend(true, [], aggregatedViewColumns, [ { label: "Package" } ]),
        rows: function(data, aggregates) {
          return aggregatedRows(aggregates, "byPackage");
        }
      },

      byClass: {
        columns: $.extend(true, [], aggregatedViewColumns, [ { label: "Class" } ]),
        rows: function(data, aggregates) {
          return aggregatedRows(aggregates, "byClass");
        }
      }
    };

    function column(id, type, label) {
      return {
        id: id,
        sortable: true,
        type: type,
        label: label,
        renderer: function(value, html) {
          html.push(escape(value));
        }
      };
    }

    function numericColumn(id, label) {
      return column(id, "numeric", label);
    }

    function aggregatedRows(aggregates, aggregate) {
      var rows = [];
      $.each(aggregates.statuses[aggregate], function(signature, statuses) {
        rows.push({
          signature: signature,
          count: aggregates.counts[aggregate][signature],
          result: { statuses: statuses, total: aggregates.counts[aggregate][signature] },
          pass: statuses[OK] || 0,
          ignored: (statuses[IGNORED] || 0) + (statuses[IGNORED_ASSUMPTION] || 0),
          error: statuses[ERROR] || 0,
          failed: statuses[FAILURE] || 0,
          time: aggregates.times[aggregate][signature]
        });
      });
      return rows;
    }
  })();

  var $table, $tools;
  var data = suites, aggregates;
  var currentView = "packages", currentOrder = { column: "signature", ascending: true };
  
  // Initialize the table
  $(document).ready(function() {

    // Split method names into semantic parts
    eachTest(data, function(test) {
      var methodSplit = test.description.methodName.split(" ");
      test.description.methodName = methodSplit[0];
      test.description.methodExtras = methodSplit[1];
      
      var classSplit = test.description.className.split("\.");
      test.description.className = classSplit.pop();
      test.description.packageName = classSplit.join(".");
    });
    
    // Create aggregations
    var counts = aggregate(data, testCount, { "global": global, "byStatus": byStatus, "byPackage": byPackage, "byClass": byClass });
    var times = aggregate(data, totalTime, { "global": global, "bySlave": bySlave, "byPackage": byPackage, "byClass": byClass });
    var statuses = aggregate(data, testCountByStatus, { "byPackage": byPackage, "byClass": byClass });
    aggregates = {
      counts: counts,
      times: times,
      statuses: statuses
    };

    // Generate markup
    var $summary = $("#summary");
    var $results = $("#results");

    // Executive summary
    var html = "";
    if ((counts.byStatus[FAILURE] || 0) == 0 && (counts.byStatus[ERROR] || 0) == 0) {
      if (counts.byStatus[OK] == counts.global) {
        html = "All tests passed.";
      } else if ((counts.byStatus[OK] || 0) > 0) {
        html = tmpl("<strong>No failures</strong>, #{passed} passed, #{ignored} ignored.", {
          passed: countText(counts.byStatus[OK], "test"),
          ignored: countText((counts.byStatus[IGNORED] || 0) + (counts.byStatus[IGNORED_ASSUMPTION] || 0), "test")
        });
      }
    }
    $("<p />").html(html).appendTo($summary);

    $("<p />").html(tmpl("\
        #{tests} executed in\
        #{time} ms on\
        <a href='#'>#{slaves}</a>.", {
        tests: countText(counts.global || 0, "test"),
        time: times.global,
        slaves: countText(keys(times.bySlave).length, "slave")
      })).appendTo($summary);

    // Status bar
    $summary.append($(statusbar(counts.byStatus, counts.global)));

    // Results table tools
    $tools = $("<div id='tools'>\
      view: <a href='#packages'>packages</a> <a href='#classes'>classes</a> <a href='#methods'>methods</a>\
    </div>").appendTo($results);


    // Results table
    $table = $("<table />").appendTo($results);

    // Bind listeners through delegation
    $tools.on("click", "a", function() {
      currentView = $(this).attr("href").substring(1);
      refresh();
      return false;
    });

    $table.on("click", "th.sortable", function() {
      var newSort = $(this).data("column");
      if (currentOrder.column == newSort) {
        currentOrder.ascending = !currentOrder.ascending;
      } else {
        currentOrder.column = newSort;
        currentOrder.ascending = true;
      }
      refresh();
      return false;
    });

    // Set the default view
    refresh();
    return this;
  });
  // Shows the requested view

  function refresh() {
    switch (currentView) {
      case "packages":
        $table.html(table(tables.byPackage, data, aggregates, currentOrder));
        break;

      case "classes":
        $table.html(table(tables.byClass, data, aggregates, currentOrder));
        break;
    }
    $tools.find("a").removeClass("active").filter("[href^=#" + currentView + "]").addClass("active");
  }

  // Renders contents of a table according to the provided spec
  function table(spec, data, aggregates, order) {
    var html = [ ];

    var orderColumn = map(spec.columns, function(c) { return c.id; })[order.column] || spec.columns[0];

    // Render column headers
    html.push("<thead>");
    html.push("<tr>");
    $.each(spec.columns, function(i, column) {
      html.push(tmpl("<th class='#{type} #{id} #{sort} #{sortable}' data-column='#{id}'><span>#{label}</span></th>", {
        type: column.type,
        id: column.id,
        sort: column.id == orderColumn.id ? (order.ascending ? "asc" : "desc") : "",
        label: column.label,
        sortable: column.sortable ? "sortable" : ""
      }));
    });
    html.push("</tr>");
    html.push("</thead>");

    // Get the data
    var rows = spec.rows(data, aggregates);

    // Sort the data
    var ordering = orderColumn.sorting || function(a, b) { return a > b ? 1 : b > a ? -1 : 0; };
    rows.sort(function(a, b) {
      return ordering(a[orderColumn.id], b[orderColumn.id]) * (order.ascending ? 1 : -1);
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
  
  function statusbar(counts, total) {
    var html = [];
    html.push("<ul class='statusbar'>");
    for (var i = 0; i < statusOrder.length; i++) {
      var status = statusOrder[i];
      var count = counts[status];
      if (count > 0) {
        html.push(tmpl("<li class='#{status}' style='width: #{pct}%' title='#{count} #{label}'></li>", {
          status: status,
          label: statusLabels[status],
          pct: (100 * count) / total,
          count: count
        }));
      }
    }
    html.push("</ul>");
    return html.join("");
  };
  
  /**
   * Aggregates the data using the provided aggregation function
   * and the provided set of key transformer functions.
   */
  function aggregate(data, aggregation, keys) {
    var a = { };
    eachTest(data, function(test) {
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
    return test.description.packageName + "." + test.description.className;
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
})(jQuery);
