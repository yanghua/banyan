<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <XS>
            <XSBH>
                <xsl:value-of select="student/studentId"/>
            </XSBH>
            <XSXM>
                <xsl:value-of select="student/name"/>
            </XSXM>
            <XSXB>
                <xsl:choose>
                    <xsl:when test="sex &lt; 1">女</xsl:when>
                    <xsl:otherwise>男</xsl:otherwise>
                </xsl:choose>
            </XSXB>
            <CSNF>
                <xsl:value-of select="substring(student/birthday, 1, 4)"/>
            </CSNF>
            <CSYF>
                <xsl:value-of select="substring(student/birthday, 6, 7)"/>
            </CSYF>
            <CSRQ>
                <xsl:value-of select="substring(student/birthday, 9, 10)"/>
            </CSRQ>
            <SFTZ>
                1
            </SFTZ>
        </XS>
    </xsl:template>
</xsl:stylesheet>