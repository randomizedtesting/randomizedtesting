(function($) {
  var data;

  // This function will be called by the JSONP data file
  window.testData = function(d) {
    data = d.suites;
  }

  // Initialize the table
  $(document).ready(function() {
    var $content = $("#content");
    var $canvas = $("<canvas width='500' height='4000' id='arrows' />").appendTo($content);

    $.each(data, function(index, suite) {
      var $suitebox = $("<div id='suitebox' />").appendTo($content);
      $("<div class='suiteName'>" + suite.description.displayName + "</div>").appendTo($suitebox);
      var $outbox = $("<pre id='outbox' />").appendTo($suitebox);

      $.each(suite.executionEvents, function(index, evtobj) {
        switch (evtobj.event) {
          case "TEST_STARTED":
            // this is ignored for some reason and placed at the top-left of the <pre> tag.
            $smark = $("<span></span>").appendTo($outbox);
            $note = $("<div class='side'><div class='note start'>" +
              evtobj.test.split("(")[0] + "</div></div>").appendTo($outbox);

            var y0 = $smark.offset().top;
            var x0 = $smark.offset().left;
            var y1 = $note.offset().top;
            var x1 = x0 - 30;
            $canvas.drawBezier({
                strokeStyle: "#333",
                strokeWidth: 1,
                x1: x0, y1: y0,
                cx1: x1, cy1: y0,
                cx2: x0, cy2: y1,
                x2: x1, y2: y1
            });
            break;
          case "APPEND_STDOUT":
            $("<span class='out'>" + evtobj.content + "</span>").appendTo($outbox);
            break;
          case "APPEND_STDERR":
            $("<span class='err'>" + evtobj.content + "</span>").appendTo($outbox);
            break;
          case "TEST_FINISHED":
            $outbox = $("<pre id='outbox' />").appendTo($suitebox);
            break;
          default:
            // do nothing.
        }
      });
    });
  });
})(jQuery);
