
# platform-test-example-frontend

The Platform Test team use this project to demonstrate how to write good tests and provide guidance on testing best practices.

### File upload for UI automation

The Create an account journey includes an Upload your photo ID step.
Continue posts a multipart form to `/platform-test-example-frontend/uploadFile`.
A non-empty file smaller than 100MB is required; any file type
is accepted for testing. The file does not need to be valid photo ID; use a
synthetic test file rather than real identification.

The contents are saved into a managed temporary file and its cleanup is handled. 
After a upload request finishes, the controller explicitly deletes every received file.
Only the filename is saved in the session answers and displayed on Check your
answers, where Change lets you upload a replacement. File contents are not
stored permanently, downloadable, scanned, or sent to Upscan or another service.
Use synthetic test files, not sensitive documents.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").