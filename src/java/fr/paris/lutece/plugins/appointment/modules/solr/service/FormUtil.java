/*
 * Copyright (c) 2002-2022, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.modules.solr.service;

import fr.paris.lutece.plugins.appointment.business.category.Category;
import fr.paris.lutece.plugins.appointment.business.category.CategoryHome;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.web.dto.AppointmentFormDTO;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexerService;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.portal.service.image.ImageResourceManager;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.util.url.UrlItem;
import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;

/**
 * Utils for the Appointment Form (Item, Url, Uid ...)
 * 
 * @author Laurent Payen
 *
 */
public final class FormUtil
{

    private static final String MIN_HOURS_BEFORE_APPOINTMENT = "min_hours_before_appointment";
    private static final String FORM_ID_CATEGORY = "form_id_category";
    private static final String APPOINTMENT_ACTIVE = "appointment_active";
    private static final String URL_BASE = "url_base";
    private static final String FORM_ID_TITLE = "form_id_title";
    private static final String APPOINTMENT_MULTISLOTS = "appointment_multislots";
    private static final String APPOINTMENT_MAX_CONSECUTIVES_SLOTS = "appointment_max_consecutives_slots";
    private static final String APPOINTMENT_NB_FREE_PLACES = "appointment_nb_free_places";
    private static final String APPOINTMENT_NB_PLACES = "appointment_nb_places";
    private static final String VIEW_APPOINTMENT = "getViewAppointmentCalendar";

    private static final String FORM_ID_TITLE_SEPARATOR = "|";
    private static final String DASH = "-";
    private static final String SLASH = "/";

    public static final String PARAMETER_ID_FORM = "id_form";
    private static final String IMAGE_RESOURCE_TYPE_ID = "appointmentForm_icon";
    private static final String ICON_URL = "appointment_form_icon_url";

    /**
     * Private constructor - this class does not need to be instantiated
     */
    private FormUtil( )
    {
    }

    /**
     * Get the form Uid
     * 
     * @param nIdForm
     *            the form id
     * @return the form Uid
     */
    public static String getFormUid( int nIdForm )
    {
        return SolrIndexerService.getWebAppName( ) + Utilities.UNDERSCORE
                + Utilities.buildResourceUid( Integer.toString( nIdForm ), Utilities.RESOURCE_TYPE_APPOINTMENT );
    }

    /**
     * Get the form url
     * 
     * @param nIdForm
     *            the form id
     * @return the form url
     */
    public static String getFormUrl( int nIdForm )
    {
        UrlItem url = new UrlItem( SolrIndexerService.getBaseUrl( ) );
        url.addParameter( Utilities.PARAMETER_XPAGE, Utilities.XPAGE_APPOINTMENT );
        url.addParameter( Utilities.PARAMETER_VIEW, VIEW_APPOINTMENT );
        url.addParameter( PARAMETER_ID_FORM, nIdForm );
        return url.getUrl( );
    }
    /**
     * Build and return the default form item for Solr
     * 
     * @param appointmentForm
     *            the appointment form
     * @return the form item
     */
    public static SolrItem getDefaultFormItem( AppointmentFormDTO appointmentForm )
    {
        SolrItem item = new SolrItem( );
        item.setSummary( appointmentForm.getDescription( ) );
        item.setTitle( appointmentForm.getTitle( ) );
        item.setSite( SolrIndexerService.getWebAppName( ) );
        item.setRole( appointmentForm.getRole( ) );
        item.setXmlContent( StringUtils.EMPTY );
        Category category = CategoryHome.findByPrimaryKey( appointmentForm.getIdCategory( ) );
        if ( category != null )
        {
            item.setCategorie( Arrays.asList( category.getLabel( ) ) );
            item.addDynamicField( FORM_ID_CATEGORY, (long) category.getIdCategory( ) );
        }
        StringBuilder stringBuilder = new StringBuilder( );
        item.setContent( stringBuilder.toString( ) );
        item.addDynamicField( MIN_HOURS_BEFORE_APPOINTMENT, (long) appointmentForm.getMinTimeBeforeAppointment( ) );
        item.addDynamicFieldNotAnalysed( APPOINTMENT_ACTIVE, Boolean.toString( appointmentForm.getIsActive( ) ) );
        item.addDynamicFieldNotAnalysed( URL_BASE, SolrIndexerService.getRootUrl( ) );
        item.addDynamicFieldNotAnalysed( FORM_ID_TITLE, getFormUid( appointmentForm.getIdForm( ) ) + FORM_ID_TITLE_SEPARATOR + appointmentForm.getTitle( ) );
        item.addDynamicFieldNotAnalysed( ICON_URL, SolrIndexerService.getRootUrl( ) + ImageResourceManager.getImageUrl( IMAGE_RESOURCE_TYPE_ID, appointmentForm.getIdForm() ));
        return item;
    }

    /**
     * Build and return the Form Item for Solr
     * 
     * @param appointmentForm
     *            the Appointment Form
     * @param listSlots
     *            the list of the slots of the form
     * @return the Form Item
     */
    public static SolrItem getFormItem( AppointmentFormDTO appointmentForm, List<Slot> listSlots )
    {
        SolrItem item = getDefaultFormItem( appointmentForm );
        item.setUrl( getFormUrl( appointmentForm.getIdForm( ) ) );
        item.setUid( Utilities.buildResourceUid( Integer.toString( appointmentForm.getIdForm( ) ), Utilities.RESOURCE_TYPE_APPOINTMENT ) );
        item.setDate( appointmentForm.getDateStartValidity( ) );
        item.setType( Utilities.SHORT_NAME_APPOINTMENT );
        int freePlaces = 0;
        int places = 0;
        for ( Slot slot : listSlots )
        {
            freePlaces += Math.max( 0, slot.getNbPotentialRemainingPlaces( ) );
            places += slot.getMaxCapacity( );
        }
        if ( StringUtils.isNotEmpty( appointmentForm.getAddress( ) ) && appointmentForm.getLongitude( ) != null && appointmentForm.getLatitude( ) != null )
        {
            item.addDynamicFieldGeoloc( Utilities.SHORT_NAME_APPOINTMENT, appointmentForm.getAddress( ), appointmentForm.getLongitude( ),
                    appointmentForm.getLatitude( ), Utilities.SHORT_NAME_APPOINTMENT + DASH + freePlaces + SLASH + places );
        }
        item.addDynamicField( APPOINTMENT_MULTISLOTS, Boolean.toString( appointmentForm.getIsMultislotAppointment( ) ) );
        if( appointmentForm.getIsMultislotAppointment( ) ) {
            item.addDynamicField(APPOINTMENT_MAX_CONSECUTIVES_SLOTS, Long.valueOf(appointmentForm.getNbConsecutiveSlots()));
        }
        else {
            item.addDynamicField(APPOINTMENT_MAX_CONSECUTIVES_SLOTS,  1L);
        }
        item.addDynamicField( APPOINTMENT_NB_FREE_PLACES, Long.valueOf( freePlaces ) );
        item.addDynamicField( APPOINTMENT_NB_PLACES, Long.valueOf( places ) );
        // Date Hierarchy
        if ( appointmentForm.getDateStartValidity( ) != null )
        {
            item.setHieDate( appointmentForm.getDateStartValidity( ).toLocalDate( ).format( Utilities.HIE_DATE_FORMATTER ) );
        }
        return item;
    }

    /**
     * check if the period between the startingDate and endingDate is displayed on the calendar FO
     * 
     * @param nIdForm
     *            the form
     * @param stratingDate
     *            the starting period
     * @param endingDate
     *            the ending period
     * @return true if the period between the startingDate and endingDate is displayed on the calendar FO
     */
    public static boolean isPeriodValidToIndex( int nIdForm, LocalDate stratingDate, LocalDate endingDate )
    {

        AppointmentFormDTO form = FormService.buildAppointmentFormWithoutReservationRule( nIdForm );

        int nNbWeeksToDisplay = form.getNbWeeksToDisplay( );
        LocalDate startingDateOfDisplay = LocalDateTime.now( ).plusHours( form.getMinTimeBeforeAppointment( ) ).toLocalDate( );
        if ( form.getDateStartValidity( ) != null && startingDateOfDisplay.isBefore( form.getDateStartValidity( ).toLocalDate( ) ) )
        {
            startingDateOfDisplay = form.getDateStartValidity( ).toLocalDate( );
        }
        // Calculate the ending date of display with the nb weeks to display since today
        // We calculate the number of weeks including the current week, so it and will end to the (n) next sunday
        LocalDate endingDateOfDisplay = startingDateOfDisplay.with( WeekFields.of( LocaleService.getDefault( ) ).dayOfWeek( ), DayOfWeek.SUNDAY.getValue( ) )
                .plusWeeks( nNbWeeksToDisplay - 1L );
        if ( form.getDateEndValidity( ) != null && endingDateOfDisplay.isAfter( form.getDateEndValidity( ).toLocalDate( ) ) )
        {
            endingDateOfDisplay = form.getDateEndValidity( ).toLocalDate( );
        }

        return !( stratingDate.isAfter( endingDateOfDisplay ) || endingDate.isBefore( startingDateOfDisplay ) );

    }

}
