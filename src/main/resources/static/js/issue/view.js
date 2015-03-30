var IssueView = function(opts) {
    var self = this;
    self.contextPath = opts.contextPath;
    self.opts = opts;
    $(function() {
        self.initView();
        self.initReply();
    });
};

IssueView.prototype = {
    constructor: IssueView,

    initView: function() {
        var self = this;

        $('.issue-avatar, .participant-avatar').tooltip({placement: 'bottom'});

        $('#btnStatus').click(function() {
            $('[name="status"]').val('true');
        });

        $('[delete_id]').click(function() {
            if (!confirm('确定要删除吗？')) {
                return false;
            }
            var that = this;
            $.get(self.contextPath + '/issue/action/delete/' + $(this).attr('delete_id'), function() {
              $(that).parents('.action-row').slideUp();
            });
        });
    },

    initReply: function() {
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
