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
            alert($(this).val());
        });
    },

    _theEnd: undefined
};
