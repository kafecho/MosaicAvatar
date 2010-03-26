$(function(){
        $("#slider").slider({
        			value:8,
        			min: 8,
        			max: 48,
        			change: function(event, ui) {
        				 $(".tile").width(ui.value);
                         $(".tile").height(ui.value);
        			}
        });

        $('a[href^="http://www.twitter.com"]').attr({ target: "_blank" });

        $('#zoom').css("visibility","visible");   

        /*$(".featuredUser").hover(
            function () {
                var selector = "." + $(this).attr("screenName");
                $(selector).fadeOut("slow");
            },
            function () {
                var selector = "." + $(this).attr("screenName");
                $(selector).fadeIn("slow");
            }
        );*/
})