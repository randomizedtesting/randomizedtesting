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
            if ((a[s] || 0) != (b[s] || 0)) {
              return (b[s] || 0) - (a[s] || 0);
            }
          }
          return 0;
        },
        renderer: function(value, html) {
          html.push(statusbar(value.statuses, value.total));
        },
        type: "status"
      },

      numericColumn("pass", "Pass"),
      numericColumn("ignored", "Ign"),
      numericColumn("error", "Err"),
      numericColumn("failed", "Fail"),
      numericColumn("time", "Time [ms]")
    ];

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

    var tables = {
      byPackage: {
        columns: $.extend(true, [], aggregatedViewColumns, [ { label: "Package" } ]),
        rows: function(data, aggregates) {
          var rows = [];
          $.each(aggregates.statuses.byPackage, function(packageName, packageStatuses) {
            rows.push({
              signature: packageName,
              count: aggregates.counts.byPackage[packageName],
              result: { statuses: packageStatuses, total: aggregates.counts.byPackage[packageName] },
              pass: packageStatuses[OK] || 0,
              ignored: (packageStatuses[IGNORED] || 0) + (packageStatuses[IGNORED_ASSUMPTION] || 0),
              error: packageStatuses[ERROR] || 0,
              failed: packageStatuses[FAILURE] || 0,
              time: aggregates.times.byPackage[packageName]
            });
          });
          return rows;
        }
      }
    };
    
    return tables;
  })();
  
  // Initialize the table
  $(document).ready(function() {
    var data = suites;

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
    var counts = aggregate(data, testCount, { "global": global, "byStatus": byStatus, "byPackage": byPackage });
    var times = aggregate(data, totalTime, { "global": global, "bySlave": bySlave, "byPackage": byPackage });
    var statuses = aggregate(data, testCountByStatus, { "byPackage": byPackage });

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

    // Results table
    $("<table />").html(table(tables.byPackage, data, { counts: counts, times: times, statuses: statuses })).appendTo($results);
    return this;
  });

  // Renders contents of a table according to the provided spec
  function table(spec, data, aggregates) {
    var html = [ ];

    // Render column headers
    html.push("<thead>")
    html.push("<tr><th class='tools' colspan='", spec.columns.length, "'>view: <a href='#' class='active'>packages</a> <a href='#'>classes</a> <a href='#'>methods</a></th></tr>");
    html.push("<tr>")
    $.each(spec.columns, function(i, column) {
      html.push(tmpl("<th class='#{type} #{id}'>#{label}</th>", column));
    });
    html.push("</tr>")
    html.push("</thead>")

    // Get the data
    var rows = spec.rows(data, aggregates);

    // Sort the data
    var order = spec.columns[0].id;
    var ordering = spec.columns[0].ordering || function(a, b) { return a > b ? 1 : b > a ? -1 : 0; };
    rows.sort(function(a, b) {
      return ordering(a[order], b[order]);
    });

    // Render table rows
    html.push("<tbody>")
    $.each(rows, function(i, row) {
      html.push("<tr>");
      $.each(spec.columns, function(i, column) {
        html.push("<td class='", column.type, "'>");
        column.renderer(row[column.id], html);
        html.push("</td>");
      });
      html.push("</tr>");
    });

    html.push("</tbody>")
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
