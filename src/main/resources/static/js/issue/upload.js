var IssueUpload = function(opts) {
    var self = this;
    self.opts = opts;
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

            var imageUrl = 'http://picN.ajkimg.com/display/origin/'.replace(/picN/, 'pic' + data.image.host)
                         + data.image.id + '.jpg';
            var container = $('#' + self.opts.containerId);
            container.val(container.val() + '![](' + imageUrl + ')');
        });
    },

    _theEnd: undefined
};
