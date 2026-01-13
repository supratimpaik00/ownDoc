package app.view;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HtmlTemplates {
    private static String layout(String title, String content) {
        return ""
                + "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
                + "    <title>" + escape(title) + "</title>\n"
                + "    <style>\n"
                + "        body { font-family: Arial, sans-serif; max-width: 900px; margin: 40px auto; padding: 0 16px; background: #f4f6f8; color: #12212f; }\n"
                + "        header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }\n"
                + "        nav a { margin-right: 10px; text-decoration: none; color: #0b6bd1; }\n"
                + "        .card { background: #fff; padding: 16px; margin-bottom: 16px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.06); }\n"
                + "        .layout { display: grid; grid-template-columns: 280px 1fr; gap: 16px; align-items: start; }\n"
                + "        form { display: grid; gap: 10px; }\n"
                + "        input, textarea, button, select { padding: 10px; border-radius: 6px; border: 1px solid #d0d7de; font-size: 14px; }\n"
                + "        button { background: #0b6bd1; color: white; border: none; cursor: pointer; }\n"
                + "        button:hover { background: #084f9c; }\n"
                + "        .patient-btn { display: block; width: 100%; text-align: left; background: #e8f1ff; color: #0b3d91; border: 1px solid #c8dcff; margin-bottom: 8px; padding: 8px 10px; border-radius: 8px; }\n"
                + "        table { width: 100%; border-collapse: collapse; margin-top: 10px; }\n"
                + "        th, td { padding: 8px; border-bottom: 1px solid #e4e7eb; }\n"
                + "        .search-row { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }\n"
                + "        .search-row input { flex: 1; }\n"
                + "        .muted { color: #5f6b7a; font-size: 13px; }\n"
                + "        .history-card { background: #f9fbff; border: 1px solid #e0e7ff; padding: 10px; border-radius: 8px; margin-bottom: 8px; }\n"
                + "        .history-card h5 { margin: 0 0 4px 0; }\n"
                + "        .preview-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.55); display: none; align-items: center; justify-content: center; z-index: 999; }\n"
                + "        .preview-overlay.active { display: flex; }\n"
                + "        .preview-card { background: #fff; padding: 20px; border-radius: 10px; max-width: 860px; width: 95%; box-shadow: 0 10px 30px rgba(0,0,0,0.18); }\n"
                + "        .preview-card h3 { margin-top: 0; }\n"
                + "        .rx-sheet { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid #e6eef6; position: relative; overflow: hidden; }\n"
                + "        .rx-sheet:before { content: ''; position: absolute; top: -60px; left: -60px; width: 240px; height: 140px; background: linear-gradient(135deg, #cfe6fb, #f8fbff); border-radius: 70px; opacity: 0.9; }\n"
                + "        .rx-header { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 1; }\n"
                + "        .rx-doctor { font-size: 22px; font-weight: 700; color: #2f6fb2; }\n"
                + "        .rx-qual { letter-spacing: 2px; font-size: 12px; color: #2f6fb2; text-transform: uppercase; }\n"
                + "        .rx-symbol { font-size: 22px; font-weight: 700; color: #2f6fb2; border: 2px solid #2f6fb2; border-radius: 50%; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; }\n"
                + "        .rx-meta { margin-top: 16px; display: grid; gap: 10px; position: relative; z-index: 1; }\n"
                + "        .rx-line { display: flex; gap: 10px; align-items: flex-end; }\n"
                + "        .rx-label { min-width: 110px; font-size: 13px; color: #5b6f86; }\n"
                + "        .rx-value { flex: 1; border-bottom: 1px solid #b9c9da; padding-bottom: 2px; font-size: 14px; color: #12212f; }\n"
                + "        .rx-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }\n"
                + "        .rx-body { margin-top: 18px; position: relative; z-index: 1; }\n"
                + "        .rx-mark { font-size: 40px; font-weight: 700; color: #3c7cc0; margin-bottom: 6px; }\n"
                + "        .rx-table { width: 100%; border-collapse: collapse; }\n"
                + "        .rx-table th { text-align: left; font-size: 12px; text-transform: uppercase; color: #5b6f86; border-bottom: 1px solid #d7e0ea; padding: 6px 0; }\n"
                + "        .rx-table td { padding: 6px 0; border-bottom: 1px dashed #e1e8f0; }\n"
                + "        .rx-footer { display: flex; justify-content: flex-end; margin-top: 24px; }\n"
                + "        .rx-sign { min-width: 200px; border-bottom: 1px solid #b9c9da; text-align: center; font-size: 12px; color: #5b6f86; padding-bottom: 4px; }\n"
                + "        .actions { display: flex; gap: 10px; }\n"
                + "        .link-button { display: inline-block; padding: 10px; border-radius: 6px; background: #0b6bd1; color: #fff; text-decoration: none; }\n"
                + "        .link-button:hover { background: #084f9c; }\n"
                + "        .error { color: #b00020; font-weight: bold; }\n"
                + "        .badge { display: inline-block; padding: 4px 8px; background: #eef3ff; color: #0b6bd1; border-radius: 999px; font-size: 12px; }\n"
                + "    </style>\n"
                + "</head>\n"
                + "<body>\n"
                + content + "\n"
                + "<script>\n"
                + "function addMedicationRowByForm(form){var tbody=form.querySelector('tbody');var tr=document.createElement('tr');tr.innerHTML='<td><input name=\"medName\" placeholder=\"Medication\" required></td><td><input name=\"dosage\" placeholder=\"Dosage\" required></td><td><input name=\"days\" placeholder=\"Days\" required></td><td><button type=\"button\" onclick=\"deleteMedicationRow(this)\">Delete</button></td>';tbody.appendChild(tr);return tr;}\n"
                + "function deleteMedicationRow(btn){var tr=btn.closest('tr');if(tr){tr.remove();}}\n"
                + "function addMedicationRow(btn){addMedicationRowByForm(btn.closest('form'));}\n"
                + "function nextEmptyMedicationInputs(form){var names=[...form.querySelectorAll('input[name=\"medName\"]')];var doses=[...form.querySelectorAll('input[name=\"dosage\"]')];var days=[...form.querySelectorAll('input[name=\"days\"]')];for(var i=0;i<names.length;i++){if(names[i].value.trim()===''&&doses[i].value.trim()===''&&days[i].value.trim()===''){return {name:names[i],dose:doses[i],days:days[i]};}}var row=addMedicationRowByForm(form);var inputs=row.querySelectorAll('input');return {name:inputs[0],dose:inputs[1],days:inputs[2]};}\n"
                + "function parseMedicationSpeech(text){var raw=(text||'').trim();if(raw===''){return null;}var parts=raw.split(/,|;/).map(function(p){return p.trim();}).filter(function(p){return p!=='';});if(parts.length>=3){return {list:parts};}var numberWords='one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve';function normalizeCount(token){if(!token){return '';}var map={one:'1',two:'2',three:'3',four:'4',five:'5',six:'6',seven:'7',eight:'8',nine:'9',ten:'10',eleven:'11',twelve:'12'};var lower=token.toLowerCase();return map[lower]||token;}var daysText='';var daysMatch=raw.match(new RegExp('\\\\bfor\\\\s+([0-9]+|'+numberWords+')\\\\s*(day|days|week|weeks)\\\\b','i'));if(daysMatch){daysText=normalizeCount(daysMatch[1])+' '+daysMatch[2];}var dosageText='';var doseMatch=raw.match(new RegExp('\\\\b([0-9]+|'+numberWords+')\\\\s*(x|times?)\\\\s*(a|per)?\\\\s*day\\\\b','i'));if(doseMatch){var count=normalizeCount(doseMatch[1]);dosageText=count==='1'?'once a day':count+' times a day';}else if(/\\bonce\\s+a\\s+day\\b/i.test(raw)){dosageText='once a day';}else if(/\\btwice\\s+a\\s+day\\b/i.test(raw)){dosageText='twice a day';}else if(/\\bthrice\\s+a\\s+day\\b/i.test(raw)){dosageText='thrice a day';}else{var everyMatch=raw.match(new RegExp('\\\\bevery\\\\s+([0-9]+|'+numberWords+')\\\\s*(hour|hours)\\\\b','i'));if(everyMatch){dosageText='every '+normalizeCount(everyMatch[1])+' '+everyMatch[2];}}var name=raw.replace(new RegExp('\\\\bfor\\\\s+(?:[0-9]+|'+numberWords+')\\\\s*(day|days|week|weeks)\\\\b','ig'),'').replace(new RegExp('\\\\b(?:[0-9]+|'+numberWords+')\\\\s*(x|times?)\\\\s*(a|per)?\\\\s*day\\\\b','ig'),'').replace(/\\bonce\\s+a\\s+day\\b/ig,'').replace(/\\btwice\\s+a\\s+day\\b/ig,'').replace(/\\bthrice\\s+a\\s+day\\b/ig,'').replace(new RegExp('\\\\bevery\\\\s+(?:[0-9]+|'+numberWords+')\\\\s*(hour|hours)\\\\b','ig'),'').replace(/\\s{2,}/g,' ').trim();return {name:name,dose:dosageText,days:daysText};}\n"
                + "function fillMedicationFromSpeech(form, transcript){var cleaned=(transcript||'').trim();if(cleaned===''){return;}var parsed=parseMedicationSpeech(cleaned);if(!parsed){return;}if(parsed.list){var parts=parsed.list;var idx=0;while(idx+2<parts.length){var inputs=nextEmptyMedicationInputs(form);inputs.name.value=parts[idx];inputs.dose.value=parts[idx+1];inputs.days.value=parts[idx+2];idx+=3;}return;}var inputs=nextEmptyMedicationInputs(form);if(parsed.name){inputs.name.value=parsed.name;}if(parsed.dose){inputs.dose.value=parsed.dose;}if(parsed.days){inputs.days.value=parsed.days;}}\n"
                + "function fillMedicationFromNlp(form, transcript){var cleaned=(transcript||'').trim();if(cleaned===''){return;}if(cleaned.indexOf(',')!==-1||cleaned.indexOf(';')!==-1){fillMedicationFromSpeech(form, cleaned);return;}fetch('/nlp/medication',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:'transcript='+encodeURIComponent(cleaned)}).then(function(response){if(!response.ok){throw new Error('bad');}return response.json();}).then(function(payload){if(!payload||!payload.ok){throw new Error('bad');}var inputs=nextEmptyMedicationInputs(form);if(payload.medication){inputs.name.value=payload.medication;}if(payload.dosage){inputs.dose.value=payload.dosage;}if(payload.days){inputs.days.value=payload.days;}if(!payload.medication&&!payload.dosage&&!payload.days){throw new Error('empty');}}).catch(function(){fillMedicationFromSpeech(form, cleaned);});}\n"
                + "function startMedicationVoice(btn){var form=btn.closest('form');var Speech=window.SpeechRecognition||window.webkitSpeechRecognition;if(!Speech){alert('Voice input is not supported in this browser.');return;}var recognition=new Speech();recognition.lang='en-US';recognition.interimResults=false;recognition.maxAlternatives=1;var originalText=btn.textContent;btn.disabled=true;btn.textContent='Listening...';recognition.onresult=function(event){var transcript=(event.results&&event.results[0]&&event.results[0][0]&&event.results[0][0].transcript)||'';fillMedicationFromNlp(form, transcript);};recognition.onerror=function(){alert('Voice input failed. Please try again.');};recognition.onend=function(){btn.disabled=false;btn.textContent=originalText;};recognition.start();}\n"
                + "function startDiagnosisVoice(btn){var form=btn.closest('form');var target=form.querySelector('textarea[name=\"diagnosis\"]');if(!target){return;}var Speech=window.SpeechRecognition||window.webkitSpeechRecognition;if(!Speech){alert('Voice input is not supported in this browser.');return;}var recognition=new Speech();recognition.lang='en-US';recognition.interimResults=false;recognition.maxAlternatives=1;var originalText=btn.textContent;btn.disabled=true;btn.textContent='Listening...';recognition.onresult=function(event){var transcript=(event.results&&event.results[0]&&event.results[0][0]&&event.results[0][0].transcript)||'';target.value=transcript;};recognition.onerror=function(){alert('Voice input failed. Please try again.');};recognition.onend=function(){btn.disabled=false;btn.textContent=originalText;};recognition.start();}\n"
                + "function extractMedRows(form){var names=[...form.querySelectorAll('input[name=\"medName\"]')];var doses=[...form.querySelectorAll('input[name=\"dosage\"]')];var days=[...form.querySelectorAll('input[name=\"days\"]')];var rows=[];for(var i=0;i<names.length;i++){var name=names[i].value.trim();var dose=doses[i].value.trim();var day=days[i].value.trim();if(name!==''||dose!==''||day!==''){rows.push({n:name,d:dose,t:day});}}return rows;}\n"
                + "function buildPrescriptionPayload(form){var rows=extractMedRows(form);if(rows.length===0){alert('Please add at least one medication row.');return false;}var plan=rows.map(function(r){return r.n+' | '+r.d+' | '+r.t;}).join('\\n');form.querySelector('input[name=\"medicationPlan\"]').value=plan;form.querySelector('input[name=\"medication\"]').value=plan;return true;}\n"
                + "function printPrescription(){var sheet=document.querySelector('#preview-body .rx-sheet');if(!sheet){return;}var printWindow=window.open('','_blank','width=900,height=700');if(!printWindow){alert('Pop-up blocked. Please allow pop-ups to print.');return;}var styleTag=document.querySelector('style');var css=styleTag?styleTag.innerHTML:'';printWindow.document.open();printWindow.document.write('<!DOCTYPE html><html><head><meta charset=\"UTF-8\" /><title>Prescription</title><style>'+css+'</style></head><body>'+sheet.outerHTML+'</body></html>');printWindow.document.close();printWindow.focus();printWindow.print();printWindow.close();}\n"
                + "function previewPrescription(btn){var form=btn.closest('form');var rows=extractMedRows(form);if(rows.length===0){alert('Please add at least one medication row.');return;}var diagnosis=(form.querySelector('textarea[name=\"diagnosis\"]')||{}).value||'';var patientName=form.dataset.patientName||'';var patientEmail=form.dataset.patientEmail||'';var patientAddress=form.dataset.patientAddress||'';var patientAge=form.dataset.patientAge||'';var patientGender=form.dataset.patientGender||'';var doctor=form.dataset.doctorName||'Doctor';var qual=form.dataset.doctorQual||'';var date=new Date().toLocaleDateString();var qualText=qual||'Qualifications not provided';var ageGender=patientAge||'';if(patientGender){ageGender=ageGender?ageGender+' / '+patientGender:patientGender;}var medsHtml=rows.map(function(r){return '<tr><td>'+escapeHtml(r.n)+'</td><td>'+escapeHtml(r.d)+'</td><td>'+escapeHtml(r.t)+'</td></tr>';}).join('');var modal=document.getElementById('preview');var body=document.getElementById('preview-body');body.innerHTML='<div class=\"rx-sheet\">'+'<div class=\"rx-header\"><div><div class=\"rx-doctor\">'+escapeHtml(doctor)+'</div><div class=\"rx-qual\">'+escapeHtml(qualText)+'</div></div><div class=\"rx-symbol\">RX</div></div>'+'<div class=\"rx-meta\">'+'<div class=\"rx-line\"><span class=\"rx-label\">Patient Name</span><span class=\"rx-value\">'+escapeHtml(patientName)+'</span></div>'+'<div class=\"rx-line\"><span class=\"rx-label\">Address</span><span class=\"rx-value\">'+escapeHtml(patientAddress)+'</span></div>'+'<div class=\"rx-row\">'+'<div class=\"rx-line\"><span class=\"rx-label\">Age</span><span class=\"rx-value\">'+escapeHtml(ageGender)+'</span></div>'+'<div class=\"rx-line\"><span class=\"rx-label\">Date</span><span class=\"rx-value\">'+escapeHtml(date)+'</span></div>'+'</div>'+'<div class=\"rx-line\"><span class=\"rx-label\">Diagnosis</span><span class=\"rx-value\">'+escapeHtml(diagnosis||'N/A')+'</span></div>'+'</div>'+'<div class=\"rx-body\"><div class=\"rx-mark\">Rx</div>'+'<table class=\"rx-table\"><thead><tr><th>Medication</th><th>Dosage</th><th>Days</th></tr></thead><tbody>'+medsHtml+'</tbody></table>'+'</div>'+'<div class=\"rx-footer\"><div class=\"rx-sign\">Signature</div></div>'+'</div>'+'<div class=\"actions\" style=\"margin-top:12px;\"><button type=\"button\" onclick=\"printPrescription()\">Print</button><button type=\"button\" onclick=\"closePreview()\">Close</button></div>';modal.classList.add('active');}\n"
                + "function closePreview(){var modal=document.getElementById('preview');if(modal){modal.classList.remove('active');}}\n"
                + "function escapeHtml(str){return (str||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');}\n"
                + "</script>\n"
                + "<div id=\"preview\" class=\"preview-overlay\"><div class=\"preview-card\"><div id=\"preview-body\"></div></div></div>\n"
                + "</body>\n"
                + "</html>\n";
    }

    public static String login(String error) {
        String message = error != null ? "<p class=\"error\">" + escape(error) + "</p>" : "";
        return layout("Doctor Login", """
                <header>
                    <h2>Doctor Portal</h2>
                    <nav><a href=\"/signup\">Sign up</a></nav>
                </header>
                <div class=\"card\">
                    <h3>Login</h3>
                    """ + message + """
                    <form method=\"post\" action=\"/login\">
                        <input type=\"text\" name=\"username\" placeholder=\"Username\" required />
                        <input type=\"password\" name=\"password\" placeholder=\"Password\" required />
                        <button type=\"submit\">Login</button>
                    </form>
                </div>
                """);
    }

    public static String signup(String error) {
        String message = error != null ? "<p class=\"error\">" + escape(error) + "</p>" : "";
        return layout("Doctor Signup", """
                <header>
                    <h2>Create Account</h2>
                    <nav><a href=\"/login\">Back to login</a></nav>
                </header>
                <div class=\"card\">
                    <h3>Register</h3>
                    """ + message + """
                    <form method=\"post\" action=\"/signup\">
                        <input type=\"text\" name=\"username\" placeholder=\"Choose a username\" required />
                        <input type=\"text\" name=\"name\" placeholder=\"Doctor name\" required />
                        <input type=\"password\" name=\"password\" placeholder=\"Choose a password\" required />
                        <input type=\"text\" name=\"qualifications\" placeholder=\"Qualifications (e.g. MBBS, MD)\" required />
                        <button type=\"submit\">Create account</button>
                    </form>
                </div>
                """);
    }

    public static String adminLogin(String error) {
        String message = error != null ? "<p class=\"admin-error\">" + escape(error) + "</p>" : "";
        return layout("Admin Login", """
                <style>
                    body { max-width: none; margin: 0; padding: 0; background:
                        radial-gradient(circle at 15% 20%, rgba(16, 87, 140, 0.65), transparent 55%),
                        radial-gradient(circle at 70% 10%, rgba(9, 20, 35, 0.9), transparent 50%),
                        #05070b; color: #e6eef7; }
                    .admin-auth-shell { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
                    .admin-auth-center { width: min(420px, 100%); display: grid; gap: 18px; justify-items: center; }
                    .admin-brand { text-align: center; display: grid; gap: 6px; text-transform: lowercase; }
                    .admin-brand .word { font-size: 28px; font-weight: 600; letter-spacing: 1px; color: #e5eef9; }
                    .admin-brand .word span { color: #3ca5ff; }
                    .admin-brand .tag { font-size: 18px; letter-spacing: 0.4px; color: #8fa4bc; }
                    .admin-card { width: 100%; background: linear-gradient(180deg, rgba(28, 32, 38, 0.96), rgba(18, 20, 24, 0.94));
                        border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.06); padding: 20px 22px 24px;
                        display: grid; gap: 12px; box-shadow: 0 22px 50px rgba(0, 0, 0, 0.45); }
                    .admin-field { display: grid; gap: 6px; font-size: 12px; color: #a4b2c5; }
                    .admin-field input { background: rgba(255, 255, 255, 0.06); border: 1px solid rgba(255, 255, 255, 0.08);
                        border-radius: 8px; padding: 9px 12px; color: #e7eef7; font-size: 14px; }
                    .admin-field input:focus { outline: none; border-color: rgba(60, 165, 255, 0.7);
                        box-shadow: 0 0 0 2px rgba(60, 165, 255, 0.2); }
                    .admin-btn { margin-top: 4px; padding: 10px 12px; border: none; border-radius: 8px; font-size: 14px;
                        font-weight: 600; color: #f1f6ff; background: linear-gradient(90deg, #2b6dff, #3b8bff); cursor: pointer; }
                    .admin-btn:hover { transform: translateY(-1px); box-shadow: 0 10px 24px rgba(44, 116, 255, 0.3); }
                    .admin-error { margin: 0; font-size: 13px; color: #ff8a8a; }
                </style>
                <div class=\"admin-auth-shell\">
                    <div class=\"admin-auth-center\">
                        <div class=\"admin-brand\">
                            <span class=\"word\">chry<span>sa</span>lis</span>
                            <span class=\"tag\">Login to Super Admin</span>
                        </div>
                        <div class=\"admin-card\">
                            """ + message + """
                            <form method=\"post\" action=\"/admin/login\">
                                <label class=\"admin-field\">
                                    <span>User ID</span>
                                    <input type=\"text\" name=\"username\" placeholder=\"Enter user ID\" required />
                                </label>
                                <label class=\"admin-field\">
                                    <span>Password</span>
                                    <input type=\"password\" name=\"password\" placeholder=\"Enter password\" required />
                                </label>
                                <button type=\"submit\" class=\"admin-btn\">Login</button>
                            </form>
                        </div>
                    </div>
                </div>
                """);
    }

    public static String adminDashboard(List<Doctor> doctors, Optional<Doctor> selectedDoctor, List<Patient> patients, Map<UUID, List<DiagnosisSession>> historyByPatient, Map<UUID, String> whatsappMessages, Map<String, Long> patientCounts, int totalPatients) {
        long activeDoctors = patientCounts.values().stream().filter(count -> count != null && count > 0).count();
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <style>
                    body { max-width: none; margin: 0; padding: 0; background: #05070b; color: #e6eef7; font-family: "Trebuchet MS", "Segoe UI", sans-serif; }
                    .admin-shell { min-height: 100vh; padding: 32px 40px 60px; background:
                        radial-gradient(circle at 10% 20%, rgba(13, 64, 110, 0.6), transparent 55%),
                        radial-gradient(circle at 65% 12%, rgba(14, 20, 32, 0.95), transparent 45%),
                        #05070b; }
                    .admin-topbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
                    .admin-brand { font-size: 22px; letter-spacing: 0.8px; font-weight: 600; text-transform: lowercase; color: #f1f7ff; }
                    .admin-brand span { color: #3ca5ff; }
                    .admin-actions form { margin: 0; }
                    .admin-ghost { background: transparent; color: #c7d3e3; border: 1px solid rgba(255, 255, 255, 0.2); padding: 8px 14px; border-radius: 8px; cursor: pointer; }
                    .admin-ghost:hover { border-color: rgba(60, 165, 255, 0.7); color: #ffffff; }
                    .admin-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 26px; }
                    .stat-card { background: rgba(20, 24, 31, 0.92); border: 1px solid rgba(255, 255, 255, 0.06); border-radius: 14px; padding: 18px 20px; display: grid; gap: 8px; }
                    .stat-label { font-size: 12px; color: #9fb0c6; text-transform: uppercase; letter-spacing: 0.6px; }
                    .stat-value { font-size: 26px; font-weight: 600; color: #f7fbff; }
                    .admin-section { margin-top: 20px; }
                    .admin-section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
                    .admin-section-title { font-size: 18px; margin: 0; }
                    .admin-muted { color: #9fb0c6; font-size: 12px; margin: 4px 0 0; }
                    .admin-table-card { background: rgba(17, 20, 26, 0.95); border-radius: 14px; border: 1px solid rgba(255, 255, 255, 0.06); padding: 12px; }
                    .admin-table { width: 100%; border-collapse: collapse; font-size: 13px; }
                    .admin-table th { text-align: left; color: #9fb0c6; font-weight: 600; padding: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); }
                    .admin-table td { padding: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.04); color: #dce7f5; }
                    .admin-table tr.is-selected { background: rgba(59, 130, 246, 0.12); }
                    .admin-link { color: #cfe3ff; text-decoration: none; font-weight: 600; }
                    .admin-link:hover { color: #68b0ff; }
                    .status-pill { display: inline-block; padding: 4px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
                    .status-active { background: rgba(34, 197, 94, 0.18); color: #22c55e; }
                    .status-idle { background: rgba(148, 163, 184, 0.18); color: #94a3b8; }
                    .admin-panel { background: rgba(17, 20, 26, 0.95); border-radius: 14px; border: 1px solid rgba(255, 255, 255, 0.06); padding: 18px; }
                    .admin-patient-card { background: rgba(12, 15, 20, 0.85); border: 1px solid rgba(255, 255, 255, 0.06); border-radius: 12px; padding: 14px; margin-top: 12px; }
                    .admin-patient-title { margin: 0 0 4px 0; font-size: 14px; }
                    .admin-patient-meta { margin: 0 0 6px 0; color: #8fa4bc; font-size: 12px; }
                    .admin-info { color: #d6e1ef; font-size: 13px; }
                    .admin-action { display: inline-block; padding: 8px 12px; border-radius: 8px; background: linear-gradient(90deg, #2b6dff, #3b8bff); color: #f1f6ff; text-decoration: none; font-size: 12px; font-weight: 600; }
                    .admin-action:hover { opacity: 0.9; }
                </style>
                <div class="admin-shell">
                    <header class="admin-topbar">
                        <div class="admin-brand">chry<span>sa</span>lis</div>
                        <div class="admin-actions">
                            <form method="post" action="/admin/logout">
                                <button type="submit" class="admin-ghost">Logout</button>
                            </form>
                        </div>
                    </header>
                    <section class="admin-cards">
                        <div class="stat-card">
                            <div class="stat-label">Total Clients</div>
                            <div class="stat-value">""").append(doctors.size()).append("""
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-label">Active Clients</div>
                            <div class="stat-value">""").append(activeDoctors).append("""
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-label">Total Users</div>
                            <div class="stat-value">""").append(totalPatients).append("""
                            </div>
                        </div>
                    </section>
                    <section class="admin-section">
                        <div class="admin-section-header">
                            <div>
                                <h3 class="admin-section-title">Doctor List</h3>
                                <p class="admin-muted">All registered doctors</p>
                            </div>
                        </div>
                        <div class="admin-table-card">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th>Doctor Name</th>
                                        <th>User ID</th>
                                        <th>Qualifications</th>
                                        <th>Patients</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                """);
        if (doctors.isEmpty()) {
            sb.append("<tr><td colspan=\"5\" class=\"admin-muted\">No doctors registered yet.</td></tr>");
        } else {
            for (Doctor doctor : doctors) {
                long doctorPatients = patientCounts.getOrDefault(doctor.username(), 0L);
                String status = doctorPatients > 0 ? "Active" : "Idle";
                String statusClass = doctorPatients > 0 ? "status-active" : "status-idle";
                String rowClass = selectedDoctor.isPresent() && selectedDoctor.get().username().equals(doctor.username())
                        ? "is-selected" : "";
                sb.append("<tr class=\"").append(rowClass).append("\">")
                        .append("<td><a class=\"admin-link\" href=\"/admin?doctor=")
                        .append(escape(doctor.username()))
                        .append("\">")
                        .append(escape(doctor.name()))
                        .append("</a></td>")
                        .append("<td>").append(escape(doctor.username())).append("</td>")
                        .append("<td>").append(escape(doctor.qualifications())).append("</td>")
                        .append("<td>").append(doctorPatients).append("</td>")
                        .append("<td><span class=\"status-pill ").append(statusClass).append("\">")
                        .append(status).append("</span></td>")
                        .append("</tr>");
            }
        }
        sb.append("""
                                </tbody>
                            </table>
                        </div>
                    </section>
                    <section class="admin-section">
                """);
        if (selectedDoctor.isEmpty()) {
            sb.append("<div class=\"admin-panel\"><p class=\"admin-muted\">Select a doctor to view their patients.</p></div>");
        } else {
            Doctor doc = selectedDoctor.get();
            sb.append("<div class=\"admin-panel\">")
                    .append("<div class=\"admin-section-header\">")
                    .append("<div>")
                    .append("<h3 class=\"admin-section-title\">Patients</h3>")
                    .append("<p class=\"admin-muted\">")
                    .append(escape(doc.name()))
                    .append(" | ")
                    .append(escape(doc.qualifications()))
                    .append("</p>")
                    .append("</div>")
                    .append("</div>");
            if (patients.isEmpty()) {
                sb.append("<p class=\"admin-muted\">No patients assigned to this doctor.</p>");
            } else {
                for (Patient patient : patients) {
                    String status = patient.deliveryStatus() == null ? "" : patient.deliveryStatus();
                    String nameStyle = "";
                    if ("yes".equalsIgnoreCase(status)) {
                        nameStyle = " style=\\\"color:#22c55e;\\\"";
                    } else if ("no".equalsIgnoreCase(status)) {
                        nameStyle = " style=\\\"color:#f87171;\\\"";
                    }
                    sb.append("<div class=\"admin-patient-card\">")
                            .append("<h5 class=\"admin-patient-title\"").append(nameStyle).append(">")
                            .append(escape(patient.name())).append("</h5>")
                            .append("<p class=\"admin-patient-meta\">").append(escape(patient.email())).append(" | ")
                            .append(escape(patient.phone())).append("</p>");
                    if (patient.age() != null || (patient.gender() != null && !patient.gender().isEmpty())) {
                        sb.append("<p class=\"admin-patient-meta\">Age: ")
                                .append(patient.age() == null ? "N/A" : escape(patient.age().toString()))
                                .append(" | Gender: ")
                                .append(patient.gender() == null || patient.gender().isEmpty() ? "N/A" : escape(patient.gender()))
                                .append("</p>");
                    }
                    if (patient.address() != null && !patient.address().isEmpty()) {
                        sb.append("<p class=\"admin-info\"><strong>Address:</strong> ").append(escape(patient.address())).append("</p>");
                    }
                    if (patient.notes() != null && !patient.notes().isEmpty()) {
                        sb.append("<p class=\"admin-info\"><strong>Notes:</strong> ").append(escape(patient.notes())).append("</p>");
                    }
                    String message = whatsappMessages.get(patient.id());
                    String whatsapp = message == null ? "" : whatsappLink(patient.phone(), message);
                    if (!whatsapp.isEmpty()) {
                        sb.append("<div class=\"actions\" style=\"margin:8px 0 0 0;\">")
                                .append("<a class=\"admin-action\" href=\"").append(whatsapp)
                                .append("\" target=\"_blank\" rel=\"noopener\">Send WhatsApp message</a>")
                                .append("</div>");
                    }
                    List<DiagnosisSession> history = historyByPatient.getOrDefault(patient.id(), List.of());
                    if (history.isEmpty()) {
                        sb.append("<p class=\"admin-muted\">No diagnosis saved yet.</p>");
                    } else {
                        for (DiagnosisSession session : history) {
                            sb.append("<div style=\"margin-top:8px; padding-top:8px; border-top:1px solid rgba(255,255,255,0.08);\">")
                                    .append("<p class=\"admin-patient-meta\">").append(escape(session.createdAt().toString())).append("</p>")
                                    .append("<p class=\"admin-info\"><strong>Diagnosis:</strong><br>").append(escape(session.diagnosis())).append("</p>")
                                    .append("<p class=\"admin-info\"><strong>Plan:</strong><br>")
                                    .append(escape(session.plan()).replace("\n", "<br>"))
                                    .append("</p>")
                                    .append("</div>");
                        }
                    }
                    sb.append("</div>");
                }
            }
            sb.append("</div>");
        }
        sb.append("""
                    </section>
                </div>
                """);

        return layout("Admin Console", sb.toString());
    }

    public static String dashboard(Doctor doctor, List<Patient> patients, String error, String searchTerm, Optional<Patient> selectedPatient, List<DiagnosisSession> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("<header><div><h2>Welcome, ")
                .append(escape(doctor.name()))
                .append("</h2><p class=\"badge\">Signed in</p></div>")
                .append("<form method=\"post\" action=\"/logout\"><button type=\"submit\">Logout</button></form></header>");
        if (error != null) {
            sb.append("<p class=\"error\">").append(escape(error)).append("</p>");
        }

        sb.append("<div class=\"layout\">");

        sb.append("<section class=\"card\">")
                .append("<h3>Patients</h3>")
                .append("<form class=\"search-row\" method=\"get\" action=\"/\">")
                .append("<input type=\"text\" name=\"q\" placeholder=\"Search patients by name, email, or phone\" value=\"").append(escape(searchTerm)).append("\" />")
                .append("<button type=\"submit\">Search</button>")
                .append("</form>")
                .append("<h4>Add patient</h4>")
                .append("<form method=\"post\" action=\"/patients\">")
                .append("<input type=\"text\" name=\"name\" placeholder=\"Full name\" required />")
                .append("<input type=\"email\" name=\"email\" placeholder=\"Email\" required />")
                .append("<input type=\"tel\" name=\"phone\" placeholder=\"Phone\" required />")
                .append("<input type=\"number\" name=\"age\" placeholder=\"Age\" min=\"0\" required />")
                .append("<select name=\"gender\" required>")
                .append(genderOptions(""))
                .append("</select>")
                .append("<input type=\"text\" name=\"address\" placeholder=\"Address\" required />")
                .append("<textarea name=\"notes\" rows=\"2\" placeholder=\"Notes (optional)\"></textarea>")
                .append("<button type=\"submit\">Create patient</button>")
                .append("</form>")
                .append("<div style=\"margin-top:12px;\">");
        if (patients.isEmpty()) {
            sb.append("<p>No patients yet.</p>");
        } else {
            for (Patient patient : patients) {
                sb.append("<a class=\"patient-btn\" href=\"/?selected=").append(patient.id()).append("\">")
                        .append("<strong>").append(escape(patient.name())).append("</strong><br>")
                        .append("<span class=\"muted\">").append(escape(patient.email())).append(" | ").append(escape(patient.phone())).append("</span>")
                        .append("</a>");
            }
        }
        sb.append("</div></section>");

        sb.append("<section class=\"card\">");
        if (selectedPatient.isPresent()) {
            Patient p = selectedPatient.get();
            sb.append("<h3>").append(escape(p.name())).append("</h3>")
                    .append("<p class=\"muted\">Email: ").append(escape(p.email())).append(" | Phone: ").append(escape(p.phone())).append("</p>");
            if (p.age() != null || (p.gender() != null && !p.gender().isEmpty())) {
                sb.append("<p class=\"muted\">Age: ")
                        .append(p.age() == null ? "N/A" : escape(p.age().toString()))
                        .append(" | Gender: ")
                        .append(p.gender() == null || p.gender().isEmpty() ? "N/A" : escape(p.gender()))
                        .append("</p>");
            }
            if (p.notes() != null && !p.notes().isEmpty()) {
                sb.append("<p><strong>Notes:</strong> ").append(escape(p.notes())).append("</p>");
            }
            if (p.address() != null && !p.address().isEmpty()) {
                sb.append("<p><strong>Address:</strong> ").append(escape(p.address())).append("</p>");
            }
            sb.append("<h4>Edit patient</h4>")
                    .append("<form method=\"post\" action=\"/patients/update\">")
                    .append("<input type=\"hidden\" name=\"patientId\" value=\"").append(p.id()).append("\" />")
                    .append("<input type=\"text\" name=\"name\" value=\"").append(escape(p.name())).append("\" required />")
                    .append("<input type=\"email\" name=\"email\" value=\"").append(escape(p.email())).append("\" required />")
                    .append("<input type=\"tel\" name=\"phone\" value=\"").append(escape(p.phone())).append("\" required />")
                    .append("<input type=\"number\" name=\"age\" value=\"").append(p.age() == null ? "" : escape(p.age().toString())).append("\" min=\"0\" required />")
                    .append("<select name=\"gender\" required>")
                    .append(genderOptions(p.gender()))
                    .append("</select>")
                    .append("<input type=\"text\" name=\"address\" value=\"").append(escape(p.address())).append("\" required />")
                    .append("<textarea name=\"notes\" rows=\"2\" placeholder=\"Notes (optional)\">").append(escape(p.notes() == null ? "" : p.notes())).append("</textarea>")
                    .append("<button type=\"submit\">Save changes</button>")
                    .append("</form>")
                    .append("<form method=\"post\" action=\"/patients/delete\" onsubmit=\"return confirm('Delete this patient?');\" style=\"margin-top:8px;\">")
                    .append("<input type=\"hidden\" name=\"patientId\" value=\"").append(p.id()).append("\" />")
                    .append("<button type=\"submit\" style=\"background:#b00020;\">Delete patient</button>")
                    .append("</form>")
                    .append("<h4 style=\"margin-top:16px;\">Prescription</h4>")
                    .append("<form method=\"post\" action=\"/prescriptions\" ")
                    .append("data-patient-name=\"").append(escape(p.name())).append("\" ")
                    .append("data-patient-email=\"").append(escape(p.email())).append("\" ")
                    .append("data-patient-address=\"").append(escape(p.address())).append("\" ")
                    .append("data-patient-age=\"").append(p.age() == null ? "" : p.age()).append("\" ")
                    .append("data-patient-gender=\"").append(escape(p.gender() == null ? "" : p.gender())).append("\" ")
                    .append("data-doctor-name=\"Dr. ").append(escape(doctor.name())).append("\" ")
                    .append("data-doctor-qual=\"").append(escape(doctor.qualifications())).append("\" ")
                    .append("onsubmit=\"return buildPrescriptionPayload(this)\">")
                    .append("<input type=\"hidden\" name=\"patientId\" value=\"").append(p.id()).append("\" />")
                    .append("<input type=\"hidden\" name=\"medicationPlan\" value=\"\" />")
                    .append("<input type=\"hidden\" name=\"medication\" value=\"\" />")
                    .append("<label>Diagnosis</label>")
                    .append("<textarea name=\"diagnosis\" rows=\"2\" placeholder=\"Diagnosis\" required></textarea>")
                    .append("<button type=\"button\" onclick=\"startDiagnosisVoice(this)\">Voice to text (diagnosis)</button>")
                    .append("<table>")
                    .append("<thead><tr><th>Medication</th><th>Dosage</th><th>Days</th><th></th></tr></thead>")
                    .append("<tbody>")
                    .append("<tr>")
                    .append("<td><input name=\"medName\" placeholder=\"Medication\" required /></td>")
                    .append("<td><input name=\"dosage\" placeholder=\"Dosage\" required /></td>")
                    .append("<td><input name=\"days\" placeholder=\"Days\" required /></td>")
                    .append("<td><button type=\"button\" onclick=\"deleteMedicationRow(this)\">Delete</button></td>")
                    .append("</tr>")
                    .append("</tbody>")
                    .append("</table>")
                    .append("<p class=\"muted\">Voice tip: say Medication, dosage, days (comma-separated). You can list multiple medications.</p>")
                    .append("<div class=\"actions\">")
                    .append("<button type=\"button\" onclick=\"addMedicationRow(this)\">Add another medication</button>")
                    .append("<button type=\"button\" onclick=\"startMedicationVoice(this)\">Voice to text</button>")
                    .append("</div>")
                    .append("<div class=\"actions\">")
                    .append("<button type=\"button\" onclick=\"previewPrescription(this)\">Preview prescription</button>")
                    .append("<button type=\"submit\" formaction=\"/sessions/save\">Save diagnosis</button>")
                    .append("<button type=\"submit\">Send prescription</button>")
                    .append("</div>")
                    .append("</form>");
            sb.append("<h4 style=\"margin-top:16px;\">Diagnosis history (").append(history.size()).append(")</h4>");
            if (history.isEmpty()) {
                sb.append("<p class=\"muted\">No diagnosis saved yet.</p>");
            } else {
                for (DiagnosisSession session : history) {
                    sb.append("<div class=\"history-card\">")
                            .append("<h5>").append(escape(session.createdAt().toString())).append("</h5>")
                            .append("<p><strong>Diagnosis:</strong><br>").append(escape(session.diagnosis())).append("</p>")
                            .append("<p><strong>Plan:</strong><br>").append(escape(session.plan()).replace("\n", "<br>")).append("</p>")
                            .append("</div>");
                }
            }
        } else {
            sb.append("<p>Select a patient to view details.</p>");
        }
        sb.append("</section>");

        sb.append("</div>");

        return layout("Dashboard", sb.toString());
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String genderOptions(String current) {
        String value = current == null ? "" : current.trim();
        return "<option value=\"\">Select gender</option>"
                + genderOption("Male", value)
                + genderOption("Female", value)
                + genderOption("Other", value);
    }

    private static String genderOption(String option, String current) {
        String selected = option.equalsIgnoreCase(current) ? " selected" : "";
        return "<option value=\"" + escape(option) + "\"" + selected + ">" + escape(option) + "</option>";
    }

    private static String whatsappLink(String phone, String message) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return "";
        }
        String encoded = urlEncode(message == null ? "" : message);
        return "https://wa.me/" + digits + "?text=" + encoded;
    }

    private static String urlEncode(String value) {
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }
}
