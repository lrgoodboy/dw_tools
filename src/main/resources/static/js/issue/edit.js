var IssueEdit = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;
    self.opts = opts;

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

        $('[name="content"]').atwho({
            at: '@',
            data: self.opts.atData
        });

    },

    _theEnd: undefined
};
