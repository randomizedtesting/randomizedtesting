// Things known not to work:
// - resizing the window/ elements screws up the alignment (arcs) between labels and markers. would have to poll for position changes/ win.resize.
// - bottom floats flow beyond the container area;
// - canvas prevents copy-paste.

(function($) {
  var data;

  // This function will be called by the JSONP data file
    window.testData = function(d) {
    data = d.suites;

    var descriptionsById = { };

    // Index descriptions by id
    for (var i = 0; i < data.length; i++) {
      map(data[i].description.children);
    }

    // Link object descriptions, split method names into semantic parts
    eachTest(data, function (test) {
      var description = descriptionsById[test.description];
      test.description = description;

      description.packageClassName = description.className;

      var methodSplit = description.methodName.split(" ");
      description.methodName = methodSplit[0];
      description.methodExtras = methodSplit[1];

      description.packageClassMethodName = description.className + "." + description.methodName;

      var classSplit = description.className.split("\.");
      description.className = classSplit.pop();
      description.packageName = classSplit.join(".");
    });
    
    // Link descriptions in the event list.
    $.each(data, function(i, suite) {
      $.each(suite.executionEvents, function(j, evt) {
        if ("description" in evt) {
          evt.description = descriptionsById[evt.description];
        }
      });
    });

    function map(children) {
      for (var j = 0; j < children.length; j++) {
        var child = children[j];
        if (typeof child == 'object') {
          descriptionsById[children[j].id] = child;
          if (child.children) {
            map(child.children);
          }
        } else {
          children[j] = descriptionsById[child];
        }
      }
    }
    
    function eachTest(data, callback) {
      for (var i = 0; i < data.length; i++) {
        var tests = data[i].tests;
        for (var j = 0; j < tests.length; j++) {
          callback.call(this, tests[j]);
        }
      }
    }    
  }

  Array.prototype.peek = function() {
    return this[this.length - 1];
  };

  function redrawConnectors() {
    var $content = $("#content");
    var $canvas = $("#canvas_ovl");

    var markers = $content.find(".marker");
    var cleft = $content.offset().left;
    var ctop  = $content.offset().top;

    var canvas = $canvas.get(0);
    canvas.width = canvas.width;

    $.each(markers, function(index, marker) {
      marker = $(marker);
      var label = $(marker).data("refLabel");

      var x0 = 0.5 + label.offset().left + label.width() - cleft;
      var y0 = 0.5 + label.offset().top + label.height() / 2 - ctop;
      var x1 = 1.5 + marker.position().left;
      var y1 = 1.5 + marker.position().top;

      var ctx = canvas.getContext('2d');
      ctx.strokeStyle = "#333";
      ctx.lineWidth = .5;
      ctx.beginPath();
      ctx.moveTo(x0, y0);
      ctx.bezierCurveTo((x1 + x0) / 2, y0, 
                        (x1 + x0) / 2, y1, 
                        x1, y1);
      ctx.stroke();
    });
  }

  // Initialize the table
  $(document).ready(function() {
    var $content = $("#content");

    $.each(data, function(index, suite) {
      var $suitebox = $("<div class='suitebox' />").appendTo($content);
      $("<div class='name'>" + suite.description.displayName + "</div>").appendTo($suitebox);

      var stack = [];
      stack.push($("<pre class='outbox' />").appendTo($suitebox));

      $.each(suite.executionEvents, function(index, evtobj) {
        switch (evtobj.event) {
          case "SUITE_FAILURE":
            // Add a marker.
            var failureMarker = $("<span class='failure marker' />");
            failureMarker.appendTo(stack.peek());
            
            var label = $("<span class='suitefailure'>suite failure</span>");
            stack.peek().append(
              $("<span class='side' />").append(
                $("<div />").append(
                  label)));

            failureMarker.data("refLabel", label);
            break;

          case "TEST_STARTED":
            // Add a content wrapper for the test...
            var testArea = $("<span class='test' />").appendTo(stack.peek());
            stack.push(testArea);

            // ...and a test start marker.
            var startMarker = $("<span class='start marker' />");
            startMarker.appendTo(testArea);

            // ...and a side label.
            var label = $("<span class='test label'>" + evtobj.description.methodName + "</span>");
            testArea.append(
              $("<span class='side' />").append(
                $("<div />").append(
                  label)));

            startMarker.data("refLabel", label);
            label.data("refTestArea", testArea);
            break;

          case "APPEND_STDOUT":
            $("<span class='out'>" + evtobj.content + "</span>").appendTo(stack.peek());
            break;

          case "APPEND_STDERR":
            $("<span class='err'>" + evtobj.content + "</span>").appendTo(stack.peek());
            break;

          case "TEST_FINISHED":
            stack.pop();
            break;

          default:
            // do nothing.
        }
      });
    });

    var $canvas = $("<canvas id='canvas_ovl' />").appendTo($content);
    $canvas.attr("width",  $content.width())
           .attr("height", $content.height());

    // Attach on-hover highlights.
    var f = function() {
      $(this).data("refTestArea").toggleClass("highlight");
    };
    $('span[class ~= "label"]').hover(f, f);

    // Redraw connectors.
    redrawConnectors();

    // Refresh connectors on resize.
    var timeoutId;
    $(window).resize(function() {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(function() {
            redrawConnectors();
            $canvas.show();
        }, 250);
        $canvas.hide();
    });
  });
})(jQuery);
