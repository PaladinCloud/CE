USE `pacmandata`;

CREATE TABLE IF NOT EXISTS `cf_AssetGroupCriteriaDetails` (
  `id_` varchar(75) COLLATE utf8_bin NOT NULL DEFAULT '',
  `groupId` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `criteriaName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `attributeName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `attributeValue` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;