var IssueEdit = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;

    $(function() {
        self.init();
    });
};

IssueEdit.prototype = {
    constructor: IssueEdit,

    init: function() {
        var self = this;

        $('#btnPreview').click(function() {

            $('#tabPreview').text('加载中……');

            var data = {
                content: $('[name="content"]').val()
            };

            $.post(self.contextPath + '/issue/preview', data, function(result) {
                $('#tabPreview').html(result);
            });
        });
    },

    _theEnd: undefined
};
