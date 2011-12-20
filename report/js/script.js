(function($) {
  $(document).ready(function() {
    $("#container").junit4results(suites);
  });

  $.fn.junit4results = function(data) {
    // Constants
    var OK = "OK";
    var FAILURE = "FAILURE";
    var ERROR = "ERROR";
    var IGNORED = "IGNORED";
    var IGNORED_ASSUMPTION = "IGNORED_ASSUMPTION";

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
    var counts = aggregate(data, testCount, { "global": global, "byStatus": byStatus });
    var times = aggregate(data, totalTime, { "global": global, "bySlave": bySlave });

    // Generate markup
    var $summary = this.find("#summary");
    var $results = this.find("#results");

    var statuses = {
      OK: "OK",
      FAILURE: "FAIL",
      ERROR: "ERROR",
      IGNORED: "IGNORED",
      IGNORED_ASSUMPTION: "IGNORED"
    };
    var statusesOrder = [ OK, IGNORED_ASSUMPTION, IGNORED, ERROR, FAILURE ];

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

    // Summary statistics
    var $statusBar = $("<ul id='statusbar' />");
    for (var i = 0; i < statusesOrder.length; i++) {
      var status = statusesOrder[i];
      var count = counts.byStatus[status];
        if (count > 0) {
        $statusBar.append(tmpl("<li class='#{status}' style='width: #{pct}%' title='#{count} #{label}'></li>", {
          status: status,
          label: statuses[status],
          pct: (100 * count) / counts.global,
          count: count
        }));
      }
    }
    $statusBar.appendTo($summary);


    // Results table
    var $table = $("\
<table>\
  <thead>\
    <tr>\
      <th class='tools' colspan='3'>view: <a href='#' class='active'>packages</a> <a href='#'>classes</a> <a href='#'>methods</a></th>\
    </tr>\
    <tr>\
      <th class='asc'><span>Test</span></th>\
      <th>Result</th>\
      <th>Time</th>\
    </tr>\
  </thead>\
  <tbody></tbody>\
</table>");

    var rows = [ ];

    eachTest(data, function(test) {
      rows.push(tmpl("\
<tr>\
  <td>#{signature}</td>\
  <td class='status'><span class='#{status}'>#{statusText}</span></td>\
  <td>#{time}</td>\
</tr>", 
      {
        signature: test.description.className + "." + test.description.methodName + "()",
        status: test.status,
        statusText: statuses[test.status],
        time: test.executionTime
      }));
    });
    
    $table.find("tbody").append(rows.join("")).end().appendTo($results);

    return this;
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

  function global() {
    return undefined;
  }

  function countByStatus(test, current) {
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
    return string.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/&/g, "&amp;");
  } 
})(jQuery);
