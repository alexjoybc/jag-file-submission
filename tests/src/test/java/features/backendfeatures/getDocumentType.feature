Feature: New CSO accounts can be created if a BCeID profile is not associated with a CSO account

   @csoo
 #  @backend
   Scenario: Verify document type can be retrieved successfully
   ## Lookup api call ##
   Given Get http request is made to "LOOK_UP_API" with court level and class details
   When status is 200 and content type are verified
   Then verify response returns documentType and description