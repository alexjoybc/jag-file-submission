package ca.bc.gov.open.jag.efilingcsostarter.mappers;

import ca.bc.gov.ag.csows.filing.CivilDocument;
import ca.bc.gov.ag.csows.filing.DocumentPayments;
import ca.bc.gov.ag.csows.filing.DocumentStatuses;
import ca.bc.gov.ag.csows.filing.Milestones;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import ca.bc.gov.open.jag.efilingcommons.model.Document;
import ca.bc.gov.open.jag.efilingcommons.model.FilingPackage;
import ca.bc.gov.open.jag.efilingcsostarter.Keys;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

@Mapper
public interface DocumentMapper {




    @Mapping(target = "packageSeqNo", source = "index")
    @Mapping(target = "amendsAnotherDocumentYn", source = "document.isAmendment", defaultValue = "false")
    @Mapping(target = "clientFileNameTxt", source = "document.name")
    @Mapping(target = "documentSubtypeCd", constant = Keys.DOCUMENT_SUB_TYPE_CD)
    @Mapping(target = "documentTypeCd", source = "document.type")
    @Mapping(target = "entUserId", source = "accountDetails.clientId")
    @Mapping(target = "entDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "filePath", source = "document.serverFileName")
    @Mapping(target = "fileServer", source = "serverHost")
    @Mapping(target = "uploadStateCd", constant = Keys.SUBMISSION_UPLOAD_STATE_CD)
    @Mapping(target = "jsonObject", expression = "java(java.lang.String.valueOf(document.getData()))")
    @Mapping(target = "xmlDocumentInstanceYn", constant = Keys.XML_DOCUMENT_INSTANCE_YN)
    @Mapping(target = "milestones", source = "milestones")
    @Mapping(target = "payments", source = "payments")
    @Mapping(target = "statuses", source = "statuses")
    CivilDocument toEfilingDocument(
            Integer index,
            Document document,
            AccountDetails accountDetails,
            FilingPackage filingPackage,
            String serverHost,
            List<Milestones> milestones,
            List<DocumentPayments> payments,
            List<DocumentStatuses> statuses);

    @Mapping(target = "entUserId", source = "accountDetails.clientId")
    @Mapping(target = "entDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "milestoneDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "milestoneTypeCd",  constant = "ASUB")
    @Mapping(target = "milestoneSeqNo",  constant = "1")
    Milestones toActualSubmittedDate(AccountDetails accountDetails);

    @Mapping(target = "entUserId", source = "accountDetails.clientId")
    @Mapping(target = "entDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "milestoneDtm",  source = "xmlGregorianCalendar")
    @Mapping(target = "milestoneTypeCd",  constant = "CSUB")
    @Mapping(target = "milestoneSeqNo",  constant = "2")
    Milestones toComputedSubmittedDate(AccountDetails accountDetails, XMLGregorianCalendar xmlGregorianCalendar);

    @Mapping(target = "entUserId", source = "accountDetails.clientId")
    @Mapping(target = "entDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "paymentStatusCd",  constant = Keys.PAYMENT_STATUS_CD)
    @Mapping(target = "statutoryFeeAmt",  source = "document.statutoryFeeAmount")
    @Mapping(target = "paymentSeqNo",  constant = "1")
    DocumentPayments toEfilingDocumentPayment(Document document, AccountDetails accountDetails);

    @Mapping(target = "entUserId", source = "accountDetails.clientId")
    @Mapping(target = "entDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "statusDtm",  expression = "java(ca.bc.gov.open.jag.efilingcommons.utils.DateUtils.getCurrentXmlDate())")
    @Mapping(target = "documentStatusTypeCd",  constant = Keys.SUBMISSION_DOCUMENT_STATUS_TYPE_CD)
    @Mapping(target = "documentStatusSeqNo",  constant = "1")
    DocumentStatuses toEfilingDocumentStatus(Document document, AccountDetails accountDetails);

}
