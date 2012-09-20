var aplana_formatDate = function(inDatum){
	if (!inDatum || inDatum == null || inDatum == '') {
		return '';
	}
	return dojo.date.locale.format(new Date(inDatum), this.constraint);
};
var aplana_formatNumber = function(inDatum){
	if (!inDatum || null == inDatum || '' == inDatum || isNaN(inDatum)) {
		return '';
	}
	return inDatum;
};
