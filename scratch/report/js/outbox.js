// Things known not to work:
// - resizing the window/ elements screws up the alignment (arcs) between labels and markers. would have to poll for position changes/ win.resize.
// - bottom floats flow beyond the container area;
// - canvas prevents copy-paste.

(function($) {
  var data;

  // This function will be called by the JSONP data file
  window.testData = function(d) {
    data = d.suites;
  }

  Array.prototype.peek = function() {
    return this[this.length - 1];
  };

  function testName(testEvent) {
    var name = testEvent.test;
    return new RegExp("^[^\\ \\(]*").exec(name);
  }

  // Initialize the table
  $(document).ready(function() {
    var $content = $("#content");
    var idx = 0;

    var $canvas = $("<canvas id='canvas_ovl' />").appendTo($content);

    $.each(data, function(index, suite) {
      var $suitebox = $("<div class='suitebox' />").appendTo($content);
      $("<div class='name'>" + suite.description.displayName + "</div>").appendTo($suitebox);

      var stack = [];
      stack.push($("<pre class='outbox' />").appendTo($suitebox));

      $.each(suite.executionEvents, function(index, evtobj) {
        switch (evtobj.event) {
          case "TEST_STARTED":
            var tclz = "tclz_" + (idx++);

            // Add a content wrapper for the test...
            stack.push($("<span class='test' alt='" + tclz + "'>").appendTo(stack.peek()));
            // ...and a test start marker.
            $("<span class='start marker' alt='" + tclz + "' /></span>").appendTo(stack.peek());
            $("<span class='side'><div><span class='label' alt='" + tclz + "'>" + testName(evtobj) + "</span></div></span>").appendTo(stack.peek());
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

    $canvas.attr("width",  $content.width())
           .attr("height", $content.height());

    // We could probably just create an array of marker-label pairs and get rid of all the
    // searches here.
    
    var f = function() {
      var tclz = $(this).attr("alt");
      var testspan = $('span[class ~= "test"][alt = "' + tclz + '"]');
      $(testspan).toggleClass("highlight");
    };
    $('span[class ~= "label"]').hover(f, f);

    
    var markers = $content.find(".marker");

    var cleft = $content.offset().left;
    var ctop  = $content.offset().top;

    $.each(markers, function(index, marker) {
      marker = $(marker);
      var label = $('span[class ~= "label"][alt="' + marker.attr("alt") + '"]');
      label = $(label);

      var x0 = label.offset().left + label.width() - cleft;
      var y0 = label.offset().top + label.height() / 2 - ctop;
      var x1 = 1 + marker.position().left;
      var y1 = 1 + marker.position().top;

      $canvas.drawBezier({
        strokeStyle: "#333",
        strokeWidth: .5,
         x1: x0,  y1: y0,
        cx1: x1, cy1: y0,
        cx2: x0, cy2: y1,
         x2: x1,  y2: y1
      });
    });
  });
})(jQuery);
