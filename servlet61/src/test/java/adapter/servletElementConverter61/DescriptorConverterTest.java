package adapter.servletElementConverter61;

import java.util.Arrays;
import java.util.Collection;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * {@link adapter.servletElementConverter61.DescriptorConverter} factory + collection
 * 변환 단위 테스트.
 *
 * <p>javax {@code JspConfigDescriptor} 를 jakarta {@code JspConfigDescriptor} 어댑터로
 * 변환한 인스턴스가 (a) 원본 메서드 위임, (b) inner descriptor (JspPropertyGroup /
 * Taglib) collection 변환, (c) servlet 6.0 신규 메서드 {@code getErrorOnELNotFound}
 * 의 placeholder 반환 (null) 을 모두 만족하는지 검증한다.
 *
 * <p>Mock 정책 (FR-TEST-001 AC-3): javax descriptor 인터페이스만
 * {@code withSettings().defaultAnswer(RETURNS_SMART_NULLS)} 로 mock.
 *
 * @req FR-TEST-001
 */
class DescriptorConverterTest {

    // ----------------------------------------------------------------------
    // AC-2 위임 검증 #1: JspConfigDescriptor 변환
    //   - getJspPropertyGroups / getTaglibs 원본 호출 + collection 길이 일치
    //   - JspPropertyGroupDescriptor 어댑터의 getBuffer 가 원본 위임
    //   - TaglibDescriptor 어댑터의 getTaglibURI 가 원본 위임
    //   - servlet 6.0 신규 getErrorOnELNotFound placeholder = null
    // ----------------------------------------------------------------------
    @Test
    @DisplayName("FR-TEST-001 AC-2: JspConfigDescriptor 변환이 inner descriptor collection 위임 + 신규 메서드 placeholder 를 처리한다")
    void shouldConvertJspConfigDescriptor_AC1() {
        javax.servlet.descriptor.JspConfigDescriptor originJspConfig =
            mock(javax.servlet.descriptor.JspConfigDescriptor.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.descriptor.JspPropertyGroupDescriptor originPropertyGroup =
            mock(javax.servlet.descriptor.JspPropertyGroupDescriptor.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));
        javax.servlet.descriptor.TaglibDescriptor originTaglib =
            mock(javax.servlet.descriptor.TaglibDescriptor.class,
                withSettings().defaultAnswer(RETURNS_SMART_NULLS));

        when(originJspConfig.getJspPropertyGroups())
            .thenReturn(Arrays.asList(originPropertyGroup));
        when(originJspConfig.getTaglibs())
            .thenReturn(Arrays.asList(originTaglib));
        when(originPropertyGroup.getBuffer()).thenReturn("64kb");
        when(originTaglib.getTaglibURI()).thenReturn("http://example.com/tld");

        JspConfigDescriptor converted = DescriptorConverter.convert(originJspConfig);

        // (a) inner JspPropertyGroup collection 변환 + 원본 위임
        Collection<JspPropertyGroupDescriptor> propertyGroups = converted.getJspPropertyGroups();
        verify(originJspConfig).getJspPropertyGroups();
        assertNotNull(propertyGroups, "변환된 JspPropertyGroup collection 은 null 이 아니다");
        assertEquals(1, propertyGroups.size(), "원본 1건 → 변환된 1건");
        JspPropertyGroupDescriptor convertedPropertyGroup =
            propertyGroups.iterator().next();
        assertEquals("64kb", convertedPropertyGroup.getBuffer(),
            "변환된 JspPropertyGroup.getBuffer 는 원본을 위임한다");
        verify(originPropertyGroup).getBuffer();

        // (b) servlet 6.0 신규 추가 메서드 getErrorOnELNotFound = placeholder null (PH-004 보류)
        assertNull(convertedPropertyGroup.getErrorOnELNotFound(),
            "servlet 6.0 신규 메서드 getErrorOnELNotFound 는 placeholder 로 null 반환");

        // (c) inner Taglib collection 변환 + 원본 위임
        Collection<TaglibDescriptor> taglibs = converted.getTaglibs();
        verify(originJspConfig).getTaglibs();
        assertNotNull(taglibs, "변환된 Taglib collection 은 null 이 아니다");
        assertEquals(1, taglibs.size(), "원본 1건 → 변환된 1건");
        TaglibDescriptor convertedTaglib = taglibs.iterator().next();
        assertEquals("http://example.com/tld", convertedTaglib.getTaglibURI(),
            "변환된 Taglib.getTaglibURI 는 원본을 위임한다");
        verify(originTaglib).getTaglibURI();
    }
}
