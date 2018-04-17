/// <reference path="../jquery/1.11.3/jquery.min.js" />
$(document).ready(function () {
    $('form input[type="submit"]').attr('disabled', 'disabled');

    $.ajax({
        url: "/cart/ajax/SessionHandler.ashx",
        type: "GET",
        cache: false,
        contentType: "application/json; charset=utf-8",
        responseType: "json",
        async: true,
        context: this,
        success: function (response) {
            var input = $("<input>")
               .attr("type", "hidden")
               .attr("name", "sessionEncryptValue")
               .attr("id", "sessionEncryptValue").val(response.SessionKey);
            $('form').submit(function () {
                if (input != null && input != (undefined))
                    $('form').append($(input));
            });
            $('form input[type="submit"]').removeAttr('disabled');
        }
    });
})

