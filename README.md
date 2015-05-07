<h1>business-hours-java</h1>

This java 8 library helps dealing with business hours, such has "Monday through Friday from 9am to 6pm, and from 9am to 12pm on Saturdays".

<h2> How do I get a BusinessHours instance ?</h2>

Simply call the BusinessHours constructor:

<pre>BusinessHours businessHours = new BusinessHours("wday{Mon-Fri} hour{9am-6pm}, wday{Sat} hour{9am-12pm}");</pre>

The constructor argument is a String representation of the business hours which must adhere to the format:

<pre>sub-period[, sub-period...]</pre>

If the period is blank, then the business supposed to be always open.
<br>
A sub-period is of the form:
<pre>  scale {range [range ...]} [scale {range [range ...]}]</pre>
Scale must be one of three different scales (or their equivalent codes):
 <table summary="valid period scales">
  <tr>
    <th>Scale</th>
    <th>Scale code</th>
    <th>Valid range values</th>
  </tr>
  <tr>
    <td>wday</td>
    <td>wd</td>
    <td>1 (Monday) to 7 (Sunday) or mo, tu, we, th, fr, sa, su</td>
  </tr>
  <tr>
    <td>hour</td>
    <td>hr</td>
    <td>0-23 or 12am 1am-11am 12noon 12pm 1pm-11pm</td>
  </tr>
  <tr>
    <td>minute</td>
    <td>min</td>
    <td>0-59</td>
  </tr>
 </table>
 
 The same scale type may be specified multiple times. Additional scales simply extend the range defined by previous scales of the same type.
 <br>
 The range for a given scale must be a valid value in the form of <code>v</code> or <code>v-v</code>.
 <br>
 For the range specification <code>v-v</code>, if the second value is larger than the first value, the range wraps around.
 <br>
 <code>v</code> isn't a point in time. In the context of the hour scale, 9 specifies the time period from 9:00 am to 9:59.
 This is what most people would call 9-10.
 <p>
 Note that whitespaces can be anywhere.
 Furthermore, when using letters to specify week days, only the first two are significant and the case is not important:
 <code>Sunday</code> or <code>Sun</code> are valid specifications for <code>su</code>.
 
 <h3>Examples:</h3>
 
 To specify  business hours that go from Monday through Friday, 9am to 5pm:
 
 <pre>wd {Mon-Fri} hr {9am-4pm}</pre>
 
 To specify business hours that go from Monday through Friday, 9am to 5pm on Monday, Wednesday, and Friday, and 9am to 3pm on Tuesday and Thursday,
 use a period such as:
 
 <pre>wd {Mon Wed Fri} hr {9am-4pm}, wd{Tue Thu} hr {9am-2pm}</pre>
 
 To specify business hours open every other half-hour, use something like:
 <pre>minute { 0-29 }</pre>
 
 To specify the morning, use:
 
 <pre>hour { 12am-11am }</pre>
 
 Remember, 11am is not 11:00am, but rather 11:00am - 11:59am.
 
 <h2>What does it do ?</h2>
 
 <ul>
 <li>
    It tells you if the business is open at any given time:
    <pre>  boolean open = businessHours.isOpen(LocalDateTime.now());</pre>
 </li>
 <li>
    It tells you how long it will be before the next business opening:
    <pre>  long secondsToOpening = businessHours.timeBeforeOpening(LocalDateTime.now(), ChronoUnit.SECONDS);</pre>
  </li>
  <li>
    If you need to perform specific actions at each business opening, it gives you cron expressions that will fire at each opening time.
    You can feed them to Cron4j, Quartz or Spring to schedule whatever you fancy.
    <pre>Set&#60;String&#62; cronExpressions = businessHours.getOpeningCrons();</pre>
    For instance, <code>new BusinessHours("wday{Mon-Fri} hr{9-18}").getOpeningCrons()</code> will return a single cron expression: <code>0 9 * * 1-5</code>.
    <br>
    Consider a business open on Wednesdays and Thursdays from 20h30 to 3am. It opens on Wednesdays and Thursdays at 21h, but also on Wednesdays at midnight.<br>
    <code>new BusinessHours("wday{We-Th} hr{21-3}").getOpeningCrons()</code> will thus return two cron expressions: <code>0 0 * * 3</code> and <code>0 21 * * 3-4</code>.
  </ul>
