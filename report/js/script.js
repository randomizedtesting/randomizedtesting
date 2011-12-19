(function($) {
  $(document).ready(function() {
    $("#results").junit4results(suites);
  });

  $.fn.junit4results = function(data) {
    var $this = this;

    // Create aggregations
    var counts = aggregate(data, count, { "global": global, "byStatus": byStatus })
    console.log(counts);

    // Generate markup
    var $results = $("#results");

    var statuses = {
      OK: "OK",
      FAILURE: "FAIL",
      ERROR: "ERROR",
      IGNORED: "IGNORED",
      IGNORED_ASSUMPTION: "IGNORED"
    };
    var statusesOrder = [ "OK", "IGNORED_ASSUMPTION", "IGNORED", "ERROR", "FAILURE" ];

    // Summary statistics
    var $statusSummary = $("<ul id='summary' />");
    for (var i = 0; i < statusesOrder.length; i++) {
      var status = statusesOrder[i];
      $statusSummary.append(tmpl("<li class='#{status}' style='width: #{pct}%' title='#{count} #{label}'>&nbsp;</li>", {
        status: status,
        label: statuses[status],
        pct: (100 * counts.byStatus[status]) / counts.global,
        count: counts.byStatus[status]
      }));
    }
    $statusSummary.appendTo($results);


    // Results table
    var $table = $("\
<table>\
  <thead>\
    <tr>\
      <th>Test</th>\
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

  function global() {
    return undefined;
  }

  function countByStatus(test, current) {
    return addOne(current || { }, test.status);
  }

  function count(test, current) {
    return (current || 0) + 1;
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
