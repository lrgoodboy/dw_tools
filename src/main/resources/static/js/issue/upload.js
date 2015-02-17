var IssueUpload = function(opts) {
    var self = this;
    self.opts = $.extend({
        buttonId: 'btnUpload',
        containerId: 'txtContent'
    }, opts);
    self.init();
};

IssueUpload.prototype = {
    constructor: IssueUpload,

    init: function() {
        var self = this;

        $('#' + self.opts.buttonId).click(function() {
            $('#fileUpload').click();
        });

        $('#fileUpload').change(function() {
            if ($(this).val()) {
                $('#frmUpload').submit();
            }
        });

        $('#ifrmUpload').load(function() {

            var data = $(this).contents().text();
            if (!data) {
                return;
            }

            data = $.parseJSON(data);
            if (data.status == 'err') {
                alert('上传失败 - ' + data.error);
                return;
            } else if (data.status != 'ok') {
                return;
            }

            var imageUrl = $('#frmUpload').attr('data-ais-display')
                    .replace(/picN/, 'pic' + data.image.host)
                    .replace(/imageId/, data.image.id);
            var container = $('#' + self.opts.containerId);
            container.val(container.val() + '![](' + imageUrl + ')');
        });
    },

    _theEnd: undefined
};
